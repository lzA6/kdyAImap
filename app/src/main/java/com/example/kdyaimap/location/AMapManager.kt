package com.example.kdyaimap.location

import android.content.Context
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AMapManager @Inject constructor(
    private val context: Context
) {
    private var aMap: AMap? = null
    private var mapView: MapView? = null
    private var onMapClickListener: ((LatLng) -> Unit)? = null
    private var geocodeSearch: GeocodeSearch? = null
    private val _mapState = MutableStateFlow<MapState>(MapState.Initialized)
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()
    
    private val markers = mutableListOf<Marker>()
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()
    
    sealed class MapState {
        object Initialized : MapState()
        object LoadSuccess : MapState()
        data class LoadError(val error: String) : MapState()
        data class LocationUpdated(val location: LatLng) : MapState()
        data class MarkerAdded(val marker: Marker) : MapState()
        data class MarkerRemoved(val markerId: String) : MapState()
    }
    
    fun initializeMap(mapView: MapView) {
        try {
            this.mapView = mapView
            mapView.onCreate(null)
            aMap = mapView.map
            
            aMap?.apply {
                // 设置地图UI设置
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isCompassEnabled = true
                    isMyLocationButtonEnabled = true
                    isScaleControlsEnabled = true
                    isRotateGesturesEnabled = true
                    isTiltGesturesEnabled = true
                    isScrollGesturesEnabled = true
                    isZoomGesturesEnabled = true
                }
                
                // 设置地图类型和样式
                mapType = AMap.MAP_TYPE_NORMAL
                
                // 设置定位样式
                val myLocationStyle = MyLocationStyle().apply {
                    myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                    interval(2000)
                    showMyLocation(true)
                    strokeColor(android.graphics.Color.BLUE)
                    radiusFillColor(android.graphics.Color.argb(50, 0, 0, 255))
                    strokeWidth(2f)
                    myLocationIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                }
                setMyLocationStyle(myLocationStyle)
                
                // 启用定位
                isMyLocationEnabled = true
                
                // 设置地图监听器
                setOnMapLoadedListener {
                    _mapState.value = MapState.LoadSuccess
                }
                
                setOnMapClickListener { latLng ->
                    // 处理地图点击事件
                    onMapClickListener?.invoke(latLng)
                    false // 返回false允许其他监听器处理
                }
                
                setOnMarkerClickListener { marker ->
                    // 处理标记点击事件
                    true // 返回true表示已处理
                }
                
                // 设置默认缩放级别
                moveCamera(CameraUpdateFactory.zoomTo(12f))
            }
            
            // 初始化地理编码搜索
            geocodeSearch = GeocodeSearch(context).apply {
                setOnGeocodeSearchListener(geocodeSearchListener)
            }
            
        } catch (e: Exception) {
            _mapState.value = MapState.LoadError("地图初始化失败: ${e.message}")
        }
    }
    
    private val geocodeSearchListener = object : GeocodeSearch.OnGeocodeSearchListener {
        override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {
            if (code == 1000) {
                result?.regeocodeAddress?.let { regeocodeAddress ->
                    val address = regeocodeAddress.formatAddress ?: "未知地址"
                    val city = regeocodeAddress.city ?: "未知城市"
                    val district = regeocodeAddress.district ?: "未知区域"
                    
                    // 更新当前地址信息
                    _currentLocation.value?.let { latLng ->
                        _mapState.value = MapState.LocationUpdated(latLng)
                    }
                }
            } else {
                // 逆地理编码失败，但不影响定位功能
            }
        }
        
        override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {
            // 处理地理编码结果
        }
    }
    
    fun setCurrentLocation(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        _currentLocation.value = latLng
        
        aMap?.let { map ->
            // 移动地图中心到当前位置
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            _mapState.value = MapState.LocationUpdated(latLng)
        }
    }
    
    fun addMarker(
        latitude: Double,
        longitude: Double,
        title: String = "",
        snippet: String = "",
        iconRes: Int? = null
    ): Marker? {
        val latLng = LatLng(latitude, longitude)
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title(title)
            snippet(snippet)
            draggable(false)
            
            iconRes?.let { res ->
                icon(BitmapDescriptorFactory.fromResource(res))
            }
        }
        
        val marker = aMap?.addMarker(markerOptions)
        marker?.let {
            markers.add(it)
            _mapState.value = MapState.MarkerAdded(it)
        }
        
        return marker
    }
    
    fun addCustomMarker(
        latitude: Double,
        longitude: Double,
        title: String = "",
        snippet: String = "",
        iconPath: String? = null
    ): Marker? {
        val latLng = LatLng(latitude, longitude)
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title(title)
            snippet(snippet)
            draggable(false)
            
            iconPath?.let { path ->
                try {
                    icon(BitmapDescriptorFactory.fromPath(path))
                } catch (e: Exception) {
                    // 如果自定义图标加载失败，使用默认图标
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                }
            }
        }
        
        val marker = aMap?.addMarker(markerOptions)
        marker?.let {
            markers.add(it)
            _mapState.value = MapState.MarkerAdded(it)
        }
        
        return marker
    }
    
    fun addColoredMarker(
        latitude: Double,
        longitude: Double,
        title: String = "",
        snippet: String = "",
        color: String = "#FF5722"
    ): Marker? {
        val latLng = LatLng(latitude, longitude)
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title(title)
            snippet(snippet)
            draggable(false)
            
            try {
                val androidColor = android.graphics.Color.parseColor(color)
                icon(BitmapDescriptorFactory.defaultMarker(getHueFromColor(androidColor)))
            } catch (e: Exception) {
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }
        }
        
        val marker = aMap?.addMarker(markerOptions)
        marker?.let {
            markers.add(it)
            _mapState.value = MapState.MarkerAdded(it)
        }
        
        return marker
    }
    
    private fun getHueFromColor(color: Int): Float {
        // 简单的颜色到Hue转换
        return when (color) {
            android.graphics.Color.RED -> BitmapDescriptorFactory.HUE_RED
            android.graphics.Color.BLUE -> BitmapDescriptorFactory.HUE_BLUE
            android.graphics.Color.GREEN -> BitmapDescriptorFactory.HUE_GREEN
            android.graphics.Color.YELLOW -> BitmapDescriptorFactory.HUE_YELLOW
            android.graphics.Color.CYAN -> BitmapDescriptorFactory.HUE_CYAN
            android.graphics.Color.MAGENTA -> BitmapDescriptorFactory.HUE_MAGENTA
            android.graphics.Color.parseColor("#FF5722") -> BitmapDescriptorFactory.HUE_ORANGE
            android.graphics.Color.parseColor("#4CAF50") -> BitmapDescriptorFactory.HUE_GREEN
            android.graphics.Color.parseColor("#E91E63") -> BitmapDescriptorFactory.HUE_ROSE
            android.graphics.Color.parseColor("#2196F3") -> BitmapDescriptorFactory.HUE_BLUE
            android.graphics.Color.parseColor("#9C27B0") -> BitmapDescriptorFactory.HUE_VIOLET
            android.graphics.Color.parseColor("#795548") -> BitmapDescriptorFactory.HUE_ORANGE
            else -> BitmapDescriptorFactory.HUE_RED
        }
    }
    
    fun removeMarker(marker: Marker) {
        marker.remove()
        markers.remove(marker)
        _mapState.value = MapState.MarkerRemoved(marker.id.toString())
    }
    
    fun clearAllMarkers() {
        markers.forEach { it.remove() }
        markers.clear()
    }
    
    fun zoomToLocation(latitude: Double, longitude: Double, zoomLevel: Float = 15f) {
        val latLng = LatLng(latitude, longitude)
        aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }
    
    fun zoomToCurrentLocation() {
        _currentLocation.value?.let { latLng ->
            aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }
    
    fun enableMyLocation(enable: Boolean) {
        aMap?.isMyLocationEnabled = enable
    }
    
    fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val latLonPoint = LatLonPoint(latitude, longitude)
        val query = RegeocodeQuery(latLonPoint, 200f, GeocodeSearch.AMAP)
        geocodeSearch?.getFromLocationAsyn(query)
    }
    
    fun getMapCenter(): LatLng? {
        return aMap?.cameraPosition?.target
    }
    
    fun getMapZoom(): Float {
        return aMap?.cameraPosition?.zoom ?: 10f
    }
    
    fun setMapZoom(zoom: Float) {
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(zoom))
    }
    
    fun getMarkers(): List<Marker> {
        return markers.toList()
    }
    
    fun setOnMapClickListener(listener: (LatLng) -> Unit) {
        onMapClickListener = listener
    }
    
    // 导航功能
    fun startNavigation(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ) {
        val startLatLng = LatLng(startLatitude, startLongitude)
        val endLatLng = LatLng(endLatitude, endLongitude)
        
        // 添加起点和终点标记
        addMarker(startLatitude, startLongitude, "起点", "导航起点")
        addMarker(endLatitude, endLongitude, "终点", "导航终点")
        
        // 调整地图视角以显示整个路线
        aMap?.let { map ->
            val bounds = com.amap.api.maps.model.LatLngBounds.Builder()
                .include(startLatLng)
                .include(endLatLng)
                .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }
    
    fun clearNavigationMarkers() {
        // 清除导航相关的标记
        markers.filter { marker ->
            marker.title == "起点" || marker.title == "终点"
        }.forEach { marker ->
            removeMarker(marker)
        }
    }
    
    fun onResume() {
        mapView?.onResume()
    }
    
    fun onPause() {
        mapView?.onPause()
    }
    
    fun onSaveInstanceState(outState: android.os.Bundle) {
        mapView?.onSaveInstanceState(outState)
    }
    
    fun onDestroy() {
        mapView?.onDestroy()
        clearAllMarkers()
        aMap = null
        mapView = null
        geocodeSearch = null
    }
}