#!/bin/bash

# 登录修复测试脚本
# 用于快速验证后端API和Android应用集成

echo "🚀 开始测试登录修复..."

# 配置
BACKEND_URL="https://my-map-backend.tfai.workers.dev"
TEST_USERNAME="匿名用户anon_test$(date +%s)"
TEST_PASSWORD=""

echo "📋 测试配置:"
echo "  后端URL: $BACKEND_URL"
echo "  测试用户名: $TEST_USERNAME"
echo ""

# 测试1: 健康检查
echo "🔍 测试1: 检查后端服务状态"
curl -s -w "\n状态码: %{http_code}\n" "$BACKEND_URL/" | head -5
echo ""

# 测试2: 匿名用户登录
echo "🔍 测试2: 匿名用户登录"
LOGIN_RESPONSE=$(curl -s -X POST "$BACKEND_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$TEST_USERNAME\",\"password\":\"$TEST_PASSWORD\"}")

echo "响应: $LOGIN_RESPONSE"

# 检查响应格式
if echo "$LOGIN_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 登录成功 - 响应格式正确"
    
    # 提取token
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ ! -z "$TOKEN" ]; then
        echo "✅ Token获取成功: ${TOKEN:0:20}..."
        
        # 测试3: 验证token
        echo ""
        echo "🔍 测试3: 验证用户信息接口"
        USER_RESPONSE=$(curl -s -X GET "$BACKEND_URL/users/me" \
          -H "Authorization: Bearer $TOKEN")
        echo "用户信息: $USER_RESPONSE"
        
        if echo "$USER_RESPONSE" | grep -q '"success":true'; then
            echo "✅ 用户信息获取成功"
        else
            echo "❌ 用户信息获取失败"
        fi
    else
        echo "❌ Token获取失败"
    fi
else
    echo "❌ 登录失败 - 响应格式错误"
fi

echo ""

# 测试4: 用户注册
echo "🔍 测试4: 用户注册"
REGISTER_USERNAME="testuser_$(date +%s)"
REGISTER_RESPONSE=$(curl -s -X POST "$BACKEND_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$REGISTER_USERNAME\",\"password\":\"testpass123\",\"email\":\"test@example.com\"}")

echo "注册响应: $REGISTER_RESPONSE"

if echo "$REGISTER_RESPONSE" | grep -q '"success":true'; then
    echo "✅ 用户注册成功"
else
    echo "❌ 用户注册失败"
fi

echo ""

# 测试5: POI接口（需要认证）
echo "🔍 测试5: POI接口测试"
if [ ! -z "$TOKEN" ]; then
    POI_RESPONSE=$(curl -s -X GET "$BACKEND_URL/api/points" \
      -H "Authorization: Bearer $TOKEN")
    echo "POI响应: $POI_RESPONSE"
    
    if echo "$POI_RESPONSE" | grep -q '\['; then
        echo "✅ POI接口正常"
    else
        echo "❌ POI接口异常"
    fi
else
    echo "⚠️  跳过POI测试（无有效token）"
fi

echo ""
echo "🎉 测试完成！"
echo ""
echo "📝 下一步操作："
echo "1. 重新编译Android应用: ./gradlew clean build"
echo "2. 安装应用: ./gradlew installDebug"
echo "3. 查看日志: adb logcat | grep -E '(AuthViewModel|NetworkUserRepository)'"
echo "4. 测试匿名登录功能"
echo ""
echo "🔧 如有问题，请检查："
echo "- 后端是否正确部署"
echo "- 数据库schema是否更新"
echo "- 网络连接是否正常"
echo "- Android应用配置是否正确"