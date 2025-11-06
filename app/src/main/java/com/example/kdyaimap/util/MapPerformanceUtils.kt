package com.example.kdyaimap.util

import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * 地图性能优化工具类
 * 提供地图渲染优化、内存管理、标记点聚合等功能
 */
object MapPerformanceUtils {
    
    // 地图性能配置
    private const val MAX_VISIBLE_MARKERS = 100
    private const val CLUSTER_RADIUS = 50 // 聚合半径（米）
    private const val CACHE_SIZE = 50
    private const val CAMERA_ANIMATION_DURATION = 300 // 毫秒
    
    // 标记点缓存
    private val markerCache = mutableMapOf<String, Marker>()
    private val clusterCache = mutableMapOf<String, Marker>()
    
    // 当前可见的标记点
    private var visibleMarkers = mutableSetOf<String>()
    
    /**
     * 优化地图性能设置
     */
    fun optimizeMapPerformance(aMap: AMap) {
        aMap.apply {
            // 设置地图渲染模式
            mapType = AMap.MAP_TYPE_NORMAL
            
            // 启用地图缓存
            isMyLocationEnabled = true
            
            // 设置缩放级别限制
            minZoomLevel = 3f
            maxZoomLevel = 18f
            
            // 优化渲染性能 - 注释掉弃用的方法
            // setMapCustomEnable(false)
            
            // 禁用不必要的UI元素
            uiSettings.apply {
                isZoomControlsEnabled = false
                isCompassEnabled = false
                isMyLocationButtonEnabled = false
                isScaleControlsEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
            
            // 设置地图样式
            setMapLanguage(AMap.CHINESE)
            
            // 启用交通图层（可选）
            isTrafficEnabled = false
        }
    }
    
    /**
     * 智能添加标记点
     * 根据当前缩放级别和可见区域决定是否显示标记点
     */
    fun addMarkersSmartly(
        aMap: AMap,
        markers: List<MarkerOptions>,
        onMarkerClick: (Marker) -> Unit = {}
    ) {
        val currentZoom = aMap.cameraPosition.zoom
        val visibleRegion = aMap.projection.visibleRegion
        
        // 根据缩放级别决定显示策略
        when {
            currentZoom < 10f -> {
                // 低缩放级别：显示聚合标记
                addClusterMarkers(aMap, markers, visibleRegion, onMarkerClick)
            }
            currentZoom < 15f -> {
                // 中等缩放级别：显示部分标记
                addFilteredMarkers(aMap, markers, visibleRegion, onMarkerClick)
            }
            else -> {
                // 高缩放级别：显示所有标记
                addAllMarkers(aMap, markers, onMarkerClick)
            }
        }
    }
    
    /**
     * 添加聚合标记
     */
    private fun addClusterMarkers(
        aMap: AMap,
        markers: List<MarkerOptions>,
        visibleRegion: VisibleRegion,
        onMarkerClick: (Marker) -> Unit
    ) {
        // 清除现有标记
        clearMarkers(aMap)
        
        // 聚合标记点
        val clusters = clusterMarkers(markers, visibleRegion)
        
        clusters.forEach { cluster ->
            val clusterOptions = MarkerOptions()
                .position(cluster.center)
                .title("${cluster.count}个活动")
                .snippet("点击查看详情")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .draggable(false)
            
            val marker = aMap.addMarker(clusterOptions)
            marker?.let {
                clusterCache[cluster.id] = it
                visibleMarkers.add(cluster.id)
                
                aMap.setInfoWindowAdapter(null)
                aMap.setOnMarkerClickListener { clickedMarker ->
                    if (clickedMarker == it) {
                        onMarkerClick(it)
                        true
                    } else false
                }
            }
        }
    }
    
    /**
     * 添加过滤后的标记
     */
    private fun addFilteredMarkers(
        aMap: AMap,
        markers: List<MarkerOptions>,
        visibleRegion: VisibleRegion,
        onMarkerClick: (Marker) -> Unit
    ) {
        clearMarkers(aMap)
        
        // 过滤可见区域内的标记
        val visibleMarkersList = markers.filter { marker ->
            isMarkerInVisibleRegion(marker.position, visibleRegion)
        }.take(MAX_VISIBLE_MARKERS)
        
        visibleMarkersList.forEach { options ->
            addMarkerToMap(aMap, options, onMarkerClick)
        }
    }
    
    /**
     * 添加所有标记
     */
    private fun addAllMarkers(
        aMap: AMap,
        markers: List<MarkerOptions>,
        onMarkerClick: (Marker) -> Unit
    ) {
        clearMarkers(aMap)
        
        markers.take(MAX_VISIBLE_MARKERS).forEach { options ->
            addMarkerToMap(aMap, options, onMarkerClick)
        }
    }
    
    /**
     * 添加单个标记到地图
     */
    private fun addMarkerToMap(
        aMap: AMap,
        options: MarkerOptions,
        onMarkerClick: (Marker) -> Unit
    ) {
        val markerId = options.title + options.position.toString()
        
        // 检查缓存
        val cachedMarker = markerCache[markerId]
        if (cachedMarker != null) {
            cachedMarker.isVisible = true
            visibleMarkers.add(markerId)
            return
        }
        
        // 创建新标记
        val marker = aMap.addMarker(options)
        marker?.let {
            markerCache[markerId] = it
            visibleMarkers.add(markerId)
            
            aMap.setOnMarkerClickListener { clickedMarker ->
                if (clickedMarker == it) {
                    onMarkerClick(it)
                    true
                } else false
                }
        }
    }
    
    /**
     * 聚合标记点
     */
    private fun clusterMarkers(
        markers: List<MarkerOptions>,
        visibleRegion: VisibleRegion
    ): List<MarkerCluster> {
        val clusters = mutableListOf<MarkerCluster>()
        val processedMarkers = mutableSetOf<MarkerOptions>()
        
        markers.forEach { marker ->
            if (marker !in processedMarkers) {
                val nearbyMarkers = findNearbyMarkers(marker, markers, CLUSTER_RADIUS)
                if (nearbyMarkers.size > 1) {
                    // 创建聚合
                    val cluster = MarkerCluster(
                        id = "cluster_${clusters.size}",
                        markers = nearbyMarkers,
                        center = calculateCenter(nearbyMarkers)
                    )
                    clusters.add(cluster)
                    processedMarkers.addAll(nearbyMarkers)
                } else {
                    // 单独标记
                    val cluster = MarkerCluster(
                        id = "single_${clusters.size}",
                        markers = listOf(marker),
                        center = marker.position
                    )
                    clusters.add(cluster)
                    processedMarkers.add(marker)
                }
            }
        }
        
        return clusters
    }
    
    /**
     * 查找附近的标记
     */
    private fun findNearbyMarkers(
        target: MarkerOptions,
        allMarkers: List<MarkerOptions>,
        radius: Int
    ): List<MarkerOptions> {
        return allMarkers.filter { marker ->
            calculateDistance(target.position, marker.position) <= radius
        }
    }
    
    /**
     * 计算两点间距离（米）
     */
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)
        
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        
        return 6371000 * c // 地球半径（米）
    }
    
    /**
     * 计算中心点
     */
    private fun calculateCenter(markers: List<MarkerOptions>): LatLng {
        val avgLat = markers.map { it.position.latitude }.average()
        val avgLng = markers.map { it.position.longitude }.average()
        return LatLng(avgLat, avgLng)
    }
    
    /**
     * 检查标记是否在可见区域内
     */
    private fun isMarkerInVisibleRegion(
        position: LatLng,
        visibleRegion: VisibleRegion
    ): Boolean {
        return visibleRegion.latLngBounds.contains(position)
    }
    
    /**
     * 清除所有标记
     */
    fun clearMarkers(aMap: AMap) {
        markerCache.values.forEach { it.remove() }
        clusterCache.values.forEach { it.remove() }
        markerCache.clear()
        clusterCache.clear()
        visibleMarkers.clear()
    }
    
    /**
     * 优化相机移动
     */
    fun animateCameraSmoothly(
        aMap: AMap,
        target: LatLng,
        zoom: Float? = null,
        duration: Int = CAMERA_ANIMATION_DURATION
    ) {
        val cameraUpdate = if (zoom != null) {
            CameraUpdateFactory.newLatLngZoom(target, zoom)
        } else {
            CameraUpdateFactory.newLatLng(target)
        }
        
        aMap.animateCamera(cameraUpdate, duration.toLong(), null)
    }
    
    /**
     * 批量更新标记位置
     */
    fun updateMarkersBatch(
        aMap: AMap,
        updates: List<Pair<String, LatLng>>
    ) {
        updates.forEach { (markerId, newPosition) ->
            markerCache[markerId]?.position = newPosition
        }
    }
    
    /**
     * 内存清理
     */
    fun cleanup() {
        markerCache.clear()
        clusterCache.clear()
        visibleMarkers.clear()
    }
    
    /**
     * 获取当前性能统计
     */
    fun getPerformanceStats(): MapPerformanceStats {
        return MapPerformanceStats(
            totalMarkers = markerCache.size,
            visibleMarkers = visibleMarkers.size,
            clusterMarkers = clusterCache.size,
            cacheHitRate = if (visibleMarkers.isNotEmpty()) {
                (visibleMarkers.size - markerCache.size).toFloat() / visibleMarkers.size
            } else 0f
        )
    }
}

/**
 * 标记聚合数据类
 */
data class MarkerCluster(
    val id: String,
    val markers: List<MarkerOptions>,
    val center: LatLng,
    val count: Int = markers.size
)

/**
 * 地图性能统计数据
 */
data class MapPerformanceStats(
    val totalMarkers: Int,
    val visibleMarkers: Int,
    val clusterMarkers: Int,
    val cacheHitRate: Float
)

/**
 * 地图标记管理器
 */
class MapMarkerManager(private val aMap: AMap) {
    private val markers = mutableMapOf<String, Marker>()
    private val markerData = mutableMapOf<String, Any>()
    
    /**
     * 添加标记
     */
    fun addMarker(id: String, options: MarkerOptions, data: Any? = null): Marker? {
        // 移除现有标记
        removeMarker(id)
        
        val marker = aMap.addMarker(options)
        marker?.let {
            markers[id] = it
            data?.let { markerData[id] = data }
        }
        return marker
    }
    
    /**
     * 移除标记
     */
    fun removeMarker(id: String) {
        markers[id]?.remove()
        markers.remove(id)
        markerData.remove(id)
    }
    
    /**
     * 获取标记
     */
    fun getMarker(id: String): Marker? = markers[id]
    
    /**
     * 获取标记数据
     */
    fun getMarkerData(id: String): Any? = markerData[id]
    
    /**
     * 清除所有标记
     */
    fun clearAll() {
        markers.values.forEach { it.remove() }
        markers.clear()
        markerData.clear()
    }
    
    /**
     * 批量操作
     */
    fun batchUpdate(operations: List<MarkerOperation>) {
        operations.forEach { operation ->
            when (operation) {
                is MarkerOperation.Add -> {
                    addMarker(operation.id, operation.options, operation.data)
                }
                is MarkerOperation.Remove -> {
                    removeMarker(operation.id)
                }
                is MarkerOperation.Update -> {
                    markers[operation.id]?.apply {
                        operation.position?.let { position = it }
                        operation.title?.let { title = it }
                        operation.snippet?.let { snippet = it }
                        operation.icon?.let { setIcon(it) }
                    }
                }
            }
        }
    }
}

/**
 * 标记操作类型
 */
sealed class MarkerOperation {
    data class Add(val id: String, val options: MarkerOptions, val data: Any? = null) : MarkerOperation()
    data class Remove(val id: String) : MarkerOperation()
    data class Update(
        val id: String,
        val position: LatLng? = null,
        val title: String? = null,
        val snippet: String? = null,
        val icon: BitmapDescriptor? = null
    ) : MarkerOperation()
}