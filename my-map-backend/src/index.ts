import { Router, IRequest } from 'itty-router';
import adminHtml from './admin.html'; // 导入 HTML 文件作为文本

// 定义 Cloudflare Worker 的环境类型，包含 D1 数据库绑定
export interface Env {
	DB: D1Database;
}

// 定义兴趣点（Point of Interest）的数据结构
interface Point {
	id?: number;
	name: string;
	latitude: number;
	longitude: number;
	category: string;
	description?: string;
}

// 定义用户的数据结构
interface User {
	id?: number;
	username: string;
	passwordHash: string;
	email?: string;
	role: string;
	avatar?: string;
	bio?: string;
	phone?: string;
	isAnonymous?: boolean;
	anonymousId?: string;
	createdAt?: string;
	updatedAt?: string;
}

// 定义API响应格式
interface ApiResponse<T = any> {
	success: boolean;
	data?: T;
	message?: string;
	code?: number;
}

// 定义登录响应格式
interface LoginResponse {
	user: User;
	token: string;
}

const router = Router();

// 生成简单的JWT token（生产环境应使用更安全的方法）
function generateToken(user: User): string {
	const payload = {
		userId: user.id,
		username: user.username,
		role: user.role,
		exp: Date.now() + 24 * 60 * 60 * 1000 // 24小时过期
	};
	return btoa(JSON.stringify(payload));
}

// 验证token
function verifyToken(token: string): any {
	try {
		const payload = JSON.parse(atob(token));
		if (payload.exp < Date.now()) {
			throw new Error('Token expired');
		}
		return payload;
	} catch (e) {
		throw new Error('Invalid token');
	}
}

// 中间件：验证用户身份
async function authenticate(request: IRequest, env: Env): Promise<User | null> {
	const authHeader = request.headers.get('Authorization');
	if (!authHeader || !authHeader.startsWith('Bearer ')) {
		return null;
	}
	
	try {
		const token = authHeader.substring(7);
		const payload = verifyToken(token);
		
		const user = await env.DB.prepare(
			'SELECT * FROM Users WHERE id = ?'
		).bind(payload.userId).first<User>();
		
		return user || null;
	} catch (e) {
		return null;
	}
}

// 根路由：提供管理后台页面
router.get('/', () => {
	return new Response(adminHtml, {
		headers: { 'Content-Type': 'text/html;charset=utf-8' },
	});
});

// ==================== 用户认证相关API ====================

// 用户登录
router.post('/auth/login', async (request: IRequest, env: Env) => {
	try {
		const { username, password } = await request.json() as { username: string; password: string };
		
		if (!username || !password) {
			const response: ApiResponse = { success: false, message: '用户名和密码不能为空' };
			return new Response(JSON.stringify(response), { 
				status: 400,
				headers: { 'Content-Type': 'application/json' }
			});
		}
		
		// 查找用户
		let user = await env.DB.prepare(
			'SELECT * FROM Users WHERE username = ?'
		).bind(username).first<User>();
		
		// 如果用户不存在，创建匿名用户
		if (!user) {
			const newUser: User = {
				username,
				passwordHash: '', // 匿名用户密码为空
				role: 'STUDENT',
				isAnonymous: true,
				anonymousId: username.includes('anon_') ? username.split('_')[1] : null
			};
			
			const result = await env.DB.prepare(`
				INSERT INTO Users (username, passwordHash, role, isAnonymous, anonymousId)
				VALUES (?, ?, ?, ?, ?)
			`).bind(
				newUser.username,
				newUser.passwordHash,
				newUser.role,
				newUser.isAnonymous ? 1 : 0,
				newUser.anonymousId
			).run();
			
			user = await env.DB.prepare(
				'SELECT * FROM Users WHERE id = ?'
			).bind(result.meta.last_row_id).first<User>();
		}
		
		if (!user) {
			const response: ApiResponse = { success: false, message: '用户创建失败' };
			return new Response(JSON.stringify(response), { 
				status: 500,
				headers: { 'Content-Type': 'application/json' }
			});
		}
		
		// 生成token
		const token = generateToken(user);
		
		const loginResponse: LoginResponse = { user, token };
		const response: ApiResponse<LoginResponse> = { 
			success: true, 
			data: loginResponse,
			message: '登录成功'
		};
		
		return new Response(JSON.stringify(response), {
			headers: { 'Content-Type': 'application/json' },
		});
	} catch (e: any) {
		console.error('登录错误:', e);
		const response: ApiResponse = { success: false, message: e.message };
		return new Response(JSON.stringify(response), { 
			status: 500,
			headers: { 'Content-Type': 'application/json' }
		});
	}
});

// 用户注册
router.post('/auth/register', async (request: IRequest, env: Env) => {
	try {
		const { username, password, email, role = 'STUDENT' } = await request.json() as {
			username: string;
			password: string;
			email?: string;
			role?: string;
		};
		
		if (!username || !password) {
			const response: ApiResponse = { success: false, message: '用户名和密码不能为空' };
			return new Response(JSON.stringify(response), { 
				status: 400,
				headers: { 'Content-Type': 'application/json' }
			});
		}
		
		// 检查用户是否已存在
		const existingUser = await env.DB.prepare(
			'SELECT id FROM Users WHERE username = ?'
		).bind(username).first();
		
		if (existingUser) {
			const response: ApiResponse = { success: false, message: '用户名已存在' };
			return new Response(JSON.stringify(response), { 
				status: 409,
				headers: { 'Content-Type': 'application/json' }
			});
		}
		
		// 创建新用户（简单密码哈希，生产环境应使用bcrypt等）
		const passwordHash = btoa(password); // 简单编码，生产环境需要更安全的哈希
		
		const result = await env.DB.prepare(`
			INSERT INTO Users (username, passwordHash, email, role, isAnonymous)
			VALUES (?, ?, ?, ?, 0)
		`).bind(username, passwordHash, email, role).run();
		
		const user = await env.DB.prepare(
			'SELECT * FROM Users WHERE id = ?'
		).bind(result.meta.last_row_id).first<User>();
		
		if (!user) {
			const response: ApiResponse = { success: false, message: '用户创建失败' };
			return new Response(JSON.stringify(response), { 
				status: 500,
				headers: { 'Content-Type': 'application/json' }
			});
		}
		
		const token = generateToken(user);
		const loginResponse: LoginResponse = { user, token };
		const response: ApiResponse<LoginResponse> = { 
			success: true, 
			data: loginResponse,
			message: '注册成功'
		};
		
		return new Response(JSON.stringify(response), {
			headers: { 'Content-Type': 'application/json' },
		});
	} catch (e: any) {
		console.error('注册错误:', e);
		const response: ApiResponse = { success: false, message: e.message };
		return new Response(JSON.stringify(response), { 
			status: 500,
			headers: { 'Content-Type': 'application/json' }
		});
	}
});

// 获取当前用户信息
router.get('/users/me', async (request: IRequest, env: Env) => {
	try {
		const user = await authenticate(request, env);
		if (!user) {
			const response: ApiResponse = { success: false, message: '未授权' };
			return new Response(JSON.stringify(response), { 
				status: 401,
				headers: { 'Content-Type': 'application/json' }
			});
		}
		
		const response: ApiResponse<User> = { 
			success: true, 
			data: user 
		};
		
		return new Response(JSON.stringify(response), {
			headers: { 'Content-Type': 'application/json' },
		});
	} catch (e: any) {
		const response: ApiResponse = { success: false, message: e.message };
		return new Response(JSON.stringify(response), { 
			status: 500,
			headers: { 'Content-Type': 'application/json' }
		});
	}
});

// ==================== POI相关API ====================

// API 路由：获取所有兴趣点
router.get('/api/points', async (request: IRequest, env: Env) => {
	try {
		const { results } = await env.DB.prepare(
			'SELECT * FROM PointsOfInterest ORDER BY createdAt DESC'
		).all<Point>();
		return new Response(JSON.stringify(results), {
			headers: { 'Content-Type': 'application/json' },
		});
	} catch (e: any) {
		console.error(e);
		const response: ApiResponse = { success: false, message: e.message };
		return new Response(JSON.stringify(response), { 
			status: 500,
			headers: { 'Content-Type': 'application/json' }
		});
	}
});

// API 路由：添加一个新的兴趣点
router.post('/api/points', async (request: IRequest, env: Env) => {
	try {
		const user = await authenticate(request, env);
		if (!user) {
			const response: ApiResponse = { success: false, message: '未授权' };
			return new Response(JSON.stringify(response), { 
				status: 401,
				headers: { 'Content-Type': 'application/json' }
			});
		}
		
		const point: Point = await request.json();

		// 服务端验证
		if (!point.name || !point.latitude || !point.longitude || !point.category) {
			const response: ApiResponse = { success: false, message: '缺少必需字段' };
			return new Response(JSON.stringify(response), { 
				status: 400,
				headers: { 'Content-Type': 'application/json' }
			});
		}

		const { success } = await env.DB.prepare(
			'INSERT INTO PointsOfInterest (name, latitude, longitude, category, description) VALUES (?, ?, ?, ?, ?)'
		)
			.bind(point.name, point.latitude, point.longitude, point.category, point.description || '')
			.run();

		if (success) {
			const response: ApiResponse = { 
				success: true, 
				message: '兴趣点添加成功' 
			};
			return new Response(JSON.stringify(response), { 
				status: 201,
				headers: { 'Content-Type': 'application/json' }
			});
		} else {
			const response: ApiResponse = { success: false, message: '添加兴趣点失败' };
			return new Response(JSON.stringify(response), { 
				status: 500,
				headers: { 'Content-Type': 'application/json' }
			});
		}
	} catch (e: any) {
		console.error(e);
		const response: ApiResponse = { success: false, message: e.message };
		return new Response(JSON.stringify(response), { 
			status: 500,
			headers: { 'Content-Type': 'application/json' }
		});
	}
});

// 404 处理器
router.all('*', () => {
	const response: ApiResponse = { success: false, message: '接口不存在' };
	return new Response(JSON.stringify(response), { 
		status: 404,
		headers: { 'Content-Type': 'application/json' }
	});
});

// 导出 worker
export default {
	fetch: (request: Request, env: Env, ctx: ExecutionContext) =>
		router.handle(request, env, ctx),
};