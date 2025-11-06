package com.example.kdyaimap.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * P2P网络日志分析器 - 清理版本
 * 专门用于分析用户提供的P2P网络日志
 */
@Singleton
class P2PLogAnalyzerClean @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    // 用户提供的日志数据
    private val userLogLines = listOf(
        "2025-11-06 19:14:01.250 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkjtfdl-XYBMEF1EB915846430.6-72921543][C2S Ping] Timestamp:527336122	DripPeer.cpp:761",
        "2025-11-06 19:14:01.330 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkjtfdl-XYBMEF1EB915846430.6-72921543][S2C Pong] Timestamp:527336122	DripPeer.cpp:675",
        "2025-11-06 19:14:01.643 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11] formal=0 backup=15 prepare=0 unused=0, total_peers=15, expect peers = 14	DripTask.cpp:554",
        "2025-11-06 19:14:01.645 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] enough peers (formal=0 backup=15 prepare=0 unused=0), skip new tracker request.	DripTask.cpp:557",
        "2025-11-06 19:14:02.517 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.517 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.518 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.519 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.520 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=14, prepare=0, unused=1	DripTask.cpp:1524",
        "2025-11-06 19:14:02.520 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.	DripTask.cpp:797",
        "2025-11-06 19:14:02.521 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.521 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.522 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.522 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.522 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=13, prepare=0, unused=2	DripTask.cpp:1524",
        "2025-11-06 19:14:02.523 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0 backup=13 prepare=0 unused=2 need=1.18 (connect all peers: false)	DripTask.cpp:807",
        "2025-11-06 19:14:02.523 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.524 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.524 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] begin to connect TCP IPv4 peer addr: '112.32.117.201:29801', peer id: , decode optimized 0	DripPeer.cpp:136",
        "2025-11-06 19:14:02.529 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570] begin to connect XYP IPv4 peer addr: '112.45.217.140:6259', hostname: glbkj-XRVD9A6F9C72BDC1.5-14801570, mona_addr: 111.48.80.53:6944	DripPeer.cpp:156",
        "2025-11-06 19:14:02.529 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570][glbkj-XRVD9A6F9C72BDC1.5-14801570] params: timeout: 0, pid:glbkj-XRVD9A6F9C72BDC1.5-14801570, mode:2, addr:112.45.217.140:6259,111.48.80.53:6944, use_private_address: false	XYPConnection.cpp:23",
        "2025-11-06 19:14:02.572 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] connect success, cost time: 43	DripPeer.cpp:375",
        "2025-11-06 19:14:02.572 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD0748790CDB9E.60-68184356] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:02.628 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570] connect success, cost time: 98	DripPeer.cpp:375",
        "2025-11-06 19:14:02.628 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD9A6F9C72BDC1.5-14801570] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:02.644 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11] formal=0 backup=15 prepare=0 unused=0, total_peers=15, expect peers = 14	DripTask.cpp:554",
        "2025-11-06 19:14:02.644 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] enough peers (formal=0 backup=15 prepare=0 unused=0), skip new tracker request.	DripTask.cpp:557",
        "2025-11-06 19:14:02.680 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.680 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.680 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.681 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.681 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=14, prepare=0, unused=1	DripTask.cpp:1524",
        "2025-11-06 19:14:02.681 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.	DripTask.cpp:797",
        "2025-11-06 19:14:02.700 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.700 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.700 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.700 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.701 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=13, prepare=0, unused=2	DripTask.cpp:1524",
        "2025-11-06 19:14:02.701 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0 backup=13 prepare=0 unused=2 need=1.18 (connect all peers: false)	DripTask.cpp:807",
        "2025-11-06 19:14:02.701 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.701 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.701 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] begin to connect XYP IPv4 peer addr: '112.19.21.38:9461', hostname: glbkj-XRVD5E764D01F732.1-10027039, mona_addr: 120.221.80.162:6941	DripPeer.cpp:156",
        "2025-11-06 19:14:02.701 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039][glbkj-XRVD5E764D01F732.1-10027039] params: timeout: 0, pid:glbkj-XRVD5E764D01F732.1-10027039, mode:2, addr:112.19.21.38:9461,120.221.80.162:6941, use_private_address: false	XYPConnection.cpp:23",
        "2025-11-06 19:14:02.701 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] begin to connect XYP IPv4 peer addr: '183.221.11.155:1559', hostname: glbkj-XRVDD83AB36C8797.3-6497545, mona_addr: 111.48.149.34:6948	DripPeer.cpp:156",
        "2025-11-06 19:14:02.702 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545][glbkj-XRVDD83AB36C8797.3-6497545] params: timeout: 0, pid:glbkj-XRVDD83AB36C8797.3-6497545, mode:2, addr:183.221.11.155:1559,111.48.149.34:6948, use_private_address: false	XYPConnection.cpp:23",
        "2025-11-06 19:14:02.710 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.710 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.710 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.711 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.711 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=12, prepare=2, unused=1	DripTask.cpp:1524",
        "2025-11-06 19:14:02.711 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0 backup=12 prepare=2 unused=1 need=1.18 (connect all peers: false)	DripTask.cpp:807",
        "2025-11-06 19:14:02.711 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.711 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] begin to connect XYP IPv4 peer addr: '36.213.2.218:23575', hostname: gldyzlhj-XYBM6932A265440EE9.1-91175181, mona_addr: 223.109.147.166:6941	DripPeer.cpp:156",
        "2025-11-06 19:14:02.712 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181][gldyzlhj-XYBM6932A265440EE9.1-91175181] params: timeout: 0, pid:gldyzlhj-XYBM6932A265440EE9.1-91175181, mode:2, addr:36.213.2.218:23575,223.109.147.166:6941, use_private_address: false	XYPConnection.cpp:23",
        "2025-11-06 19:14:02.744 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] connect success, cost time: 32	DripPeer.cpp:375",
        "2025-11-06 19:14:02.744 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM6932A265440EE9.1-91175181] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:02.750 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] connect success, cost time: 49	DripPeer.cpp:375",
        "2025-11-06 19:14:02.751 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD5E764D01F732.1-10027039] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:02.761 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] connect success, cost time: 59	DripPeer.cpp:375",
        "2025-11-06 19:14:02.762 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDD83AB36C8797.3-6497545] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:02.888 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.889 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.889 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.889 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.890 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=14, prepare=0, unused=1	DripTask.cpp:1524",
        "2025-11-06 19:14:02.890 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.	DripTask.cpp:797",
        "2025-11-06 19:14:02.961 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.961 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.961 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.961 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.961 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=13, prepare=0, unused=2	DripTask.cpp:1524",
        "2025-11-06 19:14:02.962 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0 backup=13 prepare=0 unused=2 need=1.18 (connect all peers: false)	DripTask.cpp:807",
        "2025-11-06 19:14:02.962 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.962 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.962 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] begin to connect XYP IPv4 peer addr: '183.251.173.92:27282', hostname: glbkj-XRVD3FCC958C52DC.7-31920987, mona_addr: 223.109.185.37:6936	DripPeer.cpp:156",
        "2025-11-06 19:14:02.962 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987][glbkj-XRVD3FCC958C52DC.7-31920987] params: timeout: 0, pid:glbkj-XRVD3FCC958C52DC.7-31920987, mode:2, addr:183.251.173.92:27282,223.109.185.37:6936, use_private_address: false	XYPConnection.cpp:23",
        "2025-11-06 19:14:02.962 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] begin to connect TCP IPv4 peer addr: '39.130.26.196:6551', peer id: , decode optimized 0	DripPeer.cpp:136",
        "2025-11-06 19:14:02.971 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:02.971 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:02.971 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:02.971 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:02.972 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=12, prepare=2, unused=1	DripTask.cpp:1524",
        "2025-11-06 19:14:02.972 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0 backup=12 prepare=2 unused=1 need=1.18 (connect all peers: false)	DripTask.cpp:807",
        "2025-11-06 19:14:02.972 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:02.972 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] begin to connect TCP IPv4 peer addr: '58.250.244.53:24724', peer id: , decode optimized 0	DripPeer.cpp:136",
        "2025-11-06 19:14:03.024 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] connect success, cost time: 62	DripPeer.cpp:375",
        "2025-11-06 19:14:03.024 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD3FCC958C52DC.7-31920987] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:03.025 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] connect success, cost time: 60	DripPeer.cpp:375",
        "2025-11-06 19:14:03.025 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVDBB67107DAF84.2-61920412] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:03.045 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] connect success, cost time: 71	DripPeer.cpp:375",
        "2025-11-06 19:14:03.045 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][gldyzlhj-XYBM661D7A42D196DA.8-2552208] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:03.159 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:03.159 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:03.160 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:03.160 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:03.160 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=14, prepare=0, unused=1	DripTask.cpp:1524",
        "2025-11-06 19:14:03.160 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] total backup peers: 14 (f:0 + b:14 +p:0*0.85) >= expect backup peers: 14 (ppc:10 + r:3), skip prepare.	DripTask.cpp:797",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=13, prepare=0, unused=2	DripTask.cpp:1524",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0 backup=13 prepare=0 unused=2 need=1.18 (connect all peers: false)	DripTask.cpp:807",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:03.177 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] begin to connect TCP IPv4 peer addr: '223.114.169.95:2645', peer id: , decode optimized 0	DripPeer.cpp:136",
        "2025-11-06 19:14:03.180 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] begin to connect TCP IPv4 peer addr: '183.225.53.27:4381', peer id: , decode optimized 0	DripPeer.cpp:136",
        "2025-11-06 19:14:03.228 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] connect success, cost time: 47	DripPeer.cpp:375",
        "2025-11-06 19:14:03.228 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD89E2CA7CDA2C.26-35910009] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:03.257 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971][S2C Close] ErrorCode:4	DripPeer.cpp:691",
        "2025-11-06 19:14:03.257 28302-7192  xySDK                   usap64                               E  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] on drip error:304,status: 3	DripPeer.cpp:841",
        "2025-11-06 19:14:03.257 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] close with code: 304	DripPeer.cpp:892",
        "2025-11-06 19:14:03.257 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] DELETE	DripPeer.cpp:81",
        "2025-11-06 19:14:03.257 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0, backup=13, prepare=1, unused=1	DripTask.cpp:1524",
        "2025-11-06 19:14:03.258 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] peer count: formal=0 backup=13 prepare=1 unused=1 need=1.18 (connect all peers: false)	DripTask.cpp:807",
        "2025-11-06 19:14:03.258 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] CREATE	DripPeer.cpp:34",
        "2025-11-06 19:14:03.258 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] begin to connect TCP IPv4 peer addr: '117.188.23.32:17433', peer id: , decode optimized 0	DripPeer.cpp:136",
        "2025-11-06 19:14:03.266 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] connect success, cost time: 86	DripPeer.cpp:375",
        "2025-11-06 19:14:03.266 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glzqbwin32-PCWX222E055C94BE.2-8271] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:03.303 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] connect success, cost time: 42	DripPeer.cpp:375",
        "2025-11-06 19:14:03.303 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11][glbkj-XRVD4FCE4D2BC839.12-31415971] quick request mode, direct on ready.	DripPeer.cpp:378",
        "2025-11-06 19:14:03.359 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glfdxoecdy-EC01CNML23051830666-60694404][C2S Ping] Timestamp:527338232	DripPeer.cpp:761",
        "2025-11-06 19:14:03.394 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glfdxoecdy-EC01CNML23051830666-60694404][S2C Pong] Timestamp:527338232	DripPeer.cpp:675",
        "2025-11-06 19:14:03.591 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD77447F7A5425.57-39365772][C2S Ping] Timestamp:527338464	DripPeer.cpp:761",
        "2025-11-06 19:14:03.630 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11][glbkj-XRVD77447F7A5425.57-39365772][S2C Pong] Timestamp:527338464	DripPeer.cpp:675",
        "2025-11-06 19:14:03.657 28302-7192  xySDK                   usap64                               D  [KsSession 11][Drip 11] formal=0 backup=15 prepare=0 unused=0, total_peers=15, expect peers = 14	DripTask.cpp:554",
        "2025-11-06 19:14:03.657 28302-7192  xySDK                   usap64                               I  [KsSession 11][Drip 11] enough peers (formal=0 backup=15 prepare=0 unused=0), skip new tracker request.	DripTask.cpp:557"
    )
    
    /**
     * 分析用户提供的日志
     */
    suspend fun analyzeUserLogs(): LogAnalysisResultClean = withContext(Dispatchers.Default) {
        val errors = mutableListOf<LogErrorClean>()
        val events = mutableListOf<LogEventClean>()
        val metrics = mutableListOf<PerformanceMetricClean>()
        
        var dripErrorCount = 0
        var connectionSuccessCount = 0
        var connectionFailureCount = 0
        val connectionTimes = mutableListOf<Long>()
        
        for (line in userLogLines) {
            when {
                line.contains("on drip error:304") -> {
                    dripErrorCount++
                    errors.add(LogErrorClean(
                        timestamp = extractTimestamp(line),
                        type = "DRIP_ERROR_304",
                        message = "P2P连接错误304",
                        severity = "HIGH",
                        peerId = extractPeerId(line)
                    ))
                }
                
                line.contains("connect success") -> {
                    connectionSuccessCount++
                    val time = extractConnectionTime(line)
                    if (time != null) {
                        connectionTimes.add(time)
                    }
                    events.add(LogEventClean(
                        timestamp = extractTimestamp(line),
                        type = "CONNECTION_SUCCESS",
                        message = "连接成功",
                        peerId = extractPeerId(line)
                    ))
                }
                
                line.contains("CREATE") -> {
                    events.add(LogEventClean(
                        timestamp = extractTimestamp(line),
                        type = "PEER_CREATE",
                        message = "创建对等连接",
                        peerId = extractPeerId(line)
                    ))
                }
                
                line.contains("DELETE") -> {
                    events.add(LogEventClean(
                        timestamp = extractTimestamp(line),
                        type = "PEER_DELETE",
                        message = "删除对等连接",
                        peerId = extractPeerId(line)
                    ))
                }
                
                line.contains("C2S Ping") || line.contains("S2C Pong") -> {
                    events.add(LogEventClean(
                        timestamp = extractTimestamp(line),
                        type = "PING_PONG",
                        message = "心跳检测",
                        peerId = extractPeerId(line)
                    ))
                }
            }
        }
        
        // 计算性能指标
        val avgConnectionTime = if (connectionTimes.isNotEmpty()) {
            connectionTimes.average()
        } else 0.0
        
        metrics.add(PerformanceMetricClean(
            name = "平均连接时间",
            value = avgConnectionTime,
            unit = "ms"
        ))
        
        metrics.add(PerformanceMetricClean(
            name = "连接成功率",
            value = if (connectionSuccessCount + connectionFailureCount > 0) {
                (connectionSuccessCount.toDouble() / (connectionSuccessCount + connectionFailureCount)) * 100
            } else 0.0,
            unit = "%"
        ))
        
        LogAnalysisResultClean(
            errors = errors,
            events = events,
            metrics = metrics,
            summary = AnalysisSummaryClean(
                totalErrors = errors.size,
                totalEvents = events.size,
                criticalErrors = dripErrorCount,
                healthScore = calculateHealthScore(errors, events)
            )
        )
    }
    
    private fun extractTimestamp(line: String): String {
        return try {
            line.substring(0, 23)
        } catch (e: Exception) {
            "未知时间"
        }
    }
    
    private fun extractPeerId(line: String): String {
        val pattern = Pattern.compile("\\[(.*?)\\]")
        val matcher = pattern.matcher(line)
        return if (matcher.find()) {
            val match = matcher.group()
            // 提取peer ID
            if (match.contains("-")) {
                match.substring(1, match.length - 1).split("-").firstOrNull() ?: "未知"
            } else "未知"
        } else "未知"
    }
    
    private fun extractConnectionTime(line: String): Long? {
        return try {
            val timePattern = Pattern.compile("cost time: (\\d+)")
            val matcher = timePattern.matcher(line)
            if (matcher.find()) {
                matcher.group(1)?.toLong()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    private fun calculateHealthScore(errors: List<LogErrorClean>, events: List<LogEventClean>): Int {
        val totalEvents = events.size
        val errorRate = if (totalEvents > 0) errors.size.toDouble() / totalEvents else 0.0
        
        return when {
            errorRate > 0.5 -> 20
            errorRate > 0.3 -> 40
            errorRate > 0.1 -> 60
            errorRate > 0.05 -> 80
            else -> 100
        }
    }
}

// 数据类定义 - 避免重复定义
data class LogErrorClean(
    val timestamp: String,
    val type: String,
    val message: String,
    val severity: String,
    val peerId: String
)

data class LogEventClean(
    val timestamp: String,
    val type: String,
    val message: String,
    val peerId: String
)

data class PerformanceMetricClean(
    val name: String,
    val value: Double,
    val unit: String
)

data class AnalysisSummaryClean(
    val totalErrors: Int,
    val totalEvents: Int,
    val criticalErrors: Int,
    val healthScore: Int
)

data class LogAnalysisResultClean(
    val errors: List<LogErrorClean>,
    val events: List<LogEventClean>,
    val metrics: List<PerformanceMetricClean>,
    val summary: AnalysisSummaryClean
)