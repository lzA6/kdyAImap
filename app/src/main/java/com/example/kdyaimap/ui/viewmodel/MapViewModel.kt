package com.example.kdyaimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.location.AMapLocation
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.example.kdyaimap.core.model.LocationTag
import com.example.kdyaimap.core.model.MapTag
import com.example.kdyaimap.core.model.TaggedLocation
import com.example.kdyaimap.domain.usecase.*
import com.example.kdyaimap.location.AMapLocationManager
import com.example.kdyaimap.location.AMapManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getActiveTagsUseCase: GetActiveTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val updateTagUseCase: UpdateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val getLocationsWithTagsUseCase: GetLocationsWithTagsUseCase,
    private val createLocationUseCase: CreateLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val deleteLocationUseCase: DeleteLocationUseCase,
    private val getLocationsInBoundsUseCase: GetLocationsInBoundsUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val aMapLocationManager: AMapLocationManager,
    private val aMapManager: AMapManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    private val _selectedTag = MutableStateFlow<MapTag?>(null)
    val selectedTag: StateFlow<MapTag?> = _selectedTag.asStateFlow()
    
    private val _mapCenter = MutableStateFlow<LatLng?>(null)
    val mapCenter: StateFlow<LatLng?> = _mapCenter.asStateFlow()
    
    init {
        loadData()
        observeLocationEvents()
        observeMapEvents()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            combine(
                getActiveTagsUseCase(),
                getLocationsWithTagsUseCase(),
                aMapManager.currentLocation
            ) { tags, locations, currentLocation ->
                _uiState.value = _uiState.value.copy(
                    tags = tags,
                    locations = locations,
                    currentLocation = currentLocation,
                    isLoading = false
                )
                
                // 更新地图标记
                updateMapMarkers(locations)
            }.collect {}
        }
    }
    
    private fun updateMapMarkers(locations: List<LocationTag>) {
        // 清除现有标记
        aMapManager.clearAllMarkers()
        
        // 添加新标记
        locations.forEach { location ->
            try {
                val color = location.tagColor
                aMapManager.addColoredMarker(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    title = location.locationName,
                    snippet = "标签: ${location.tagName}",
                    color = color
                )
            } catch (e: Exception) {
                // 如果颜色解析失败，使用默认颜色
                aMapManager.addMarker(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    title = location.locationName,
                    snippet = "标签: ${location.tagName}"
                )
            }
        }
    }
    
    private fun observeLocationEvents() {
        viewModelScope.launch {
            aMapLocationManager.locationEvents.collect { event ->
                when (event) {
                    is AMapLocationManager.LocationEvent.LocationReceived -> {
                        handleLocationReceived(event.location)
                    }
                    is AMapLocationManager.LocationEvent.LocationError -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "定位失败: ${event.errorInfo}"
                        )
                    }
                    is AMapLocationManager.LocationEvent.LocationPermissionDenied -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "定位权限被拒绝，请在设置中开启定位权限"
                        )
                    }
                }
            }
        }
    }
    
    private fun observeMapEvents() {
        viewModelScope.launch {
            aMapManager.mapState.collect { state ->
                when (state) {
                    is AMapManager.MapState.LoadSuccess -> {
                        _uiState.value = _uiState.value.copy(
                            isMapLoaded = true
                        )
                    }
                    is AMapManager.MapState.LoadError -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "地图加载失败: ${state.error}"
                        )
                    }
                    is AMapManager.MapState.LocationUpdated -> {
                        _mapCenter.value = state.location
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun handleLocationReceived(location: AMapLocation) {
        val latLng = LatLng(location.latitude, location.longitude)
        aMapManager.setCurrentLocation(location.latitude, location.longitude)
        
        // 处理地址信息，如果没有地址则显示坐标
        val address = if (location.address?.isNotBlank() == true) {
            location.address
        } else {
            "纬度: ${String.format("%.6f", location.latitude)}, 经度: ${String.format("%.6f", location.longitude)}"
        }
        
        _uiState.value = _uiState.value.copy(
            currentLocation = latLng,
            currentAddress = address,
            isLocating = false,
            successMessage = "定位成功: $address"
        )
    }
    
    // 定位相关操作
    fun startLocation() {
        aMapLocationManager.startLocation()
        _uiState.value = _uiState.value.copy(isLocating = true)
    }
    
    fun stopLocation() {
        aMapLocationManager.stopLocation()
        _uiState.value = _uiState.value.copy(isLocating = false)
    }
    
    fun startContinuousLocation() {
        aMapLocationManager.startContinuousLocation()
    }
    
    // 标签相关操作
    fun selectTag(tag: MapTag?) {
        _selectedTag.value = tag
        _uiState.value = _uiState.value.copy(
            selectedTagId = tag?.id
        )
    }
    
    fun createTag(name: String, color: String, description: String? = null) {
        viewModelScope.launch {
            try {
                val tag = MapTag(
                    name = name,
                    color = color,
                    creatorId = 1L // 临时使用固定ID，实际应该从当前用户获取
                )
                createTagUseCase(tag)
                _uiState.value = _uiState.value.copy(
                    successMessage = "标签创建成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "创建标签失败: ${e.message}"
                )
            }
        }
    }
    
    fun updateTag(tag: MapTag) {
        viewModelScope.launch {
            try {
                updateTagUseCase(tag)
                _uiState.value = _uiState.value.copy(
                    successMessage = "标签更新成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "更新标签失败: ${e.message}"
                )
            }
        }
    }
    
    fun deleteTag(tag: MapTag) {
        viewModelScope.launch {
            try {
                deleteTagUseCase(tag)
                if (_selectedTag.value?.id == tag.id) {
                    selectTag(null)
                }
                _uiState.value = _uiState.value.copy(
                    successMessage = "标签删除成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "删除标签失败: ${e.message}"
                )
            }
        }
    }
    
    // 位置标记相关操作
    fun createLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        tagId: Long,
        description: String? = null,
        address: String? = null
    ) {
        viewModelScope.launch {
            try {
                val location = TaggedLocation(
                    tagId = tagId,
                    title = name,
                    latitude = latitude,
                    longitude = longitude,
                    description = description,
                    creatorId = 1L // 临时使用固定ID，实际应该从当前用户获取
                )
                createLocationUseCase(location)
                _uiState.value = _uiState.value.copy(
                    successMessage = "位置标记创建成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "创建位置标记失败: ${e.message}"
                )
            }
        }
    }
    
    fun updateLocation(location: TaggedLocation) {
        viewModelScope.launch {
            try {
                updateLocationUseCase(location)
                _uiState.value = _uiState.value.copy(
                    successMessage = "位置标记更新成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "更新位置标记失败: ${e.message}"
                )
            }
        }
    }
    
    fun deleteLocation(location: TaggedLocation) {
        viewModelScope.launch {
            try {
                deleteLocationUseCase(location)
                _uiState.value = _uiState.value.copy(
                    successMessage = "位置标记删除成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "删除位置标记失败: ${e.message}"
                )
            }
        }
    }
    
    // 地图操作
    fun initializeMap(mapView: MapView) {
        aMapManager.initializeMap(mapView)
        
        // 设置地图点击监听器
        aMapManager.setOnMapClickListener { latLng ->
            onMapClick(latLng)
        }
    }
    
    fun setMapClickListener(listener: (LatLng) -> Unit) {
        aMapManager.setOnMapClickListener(listener)
    }
    
    fun onMapClick(latLng: LatLng) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = latLng,
            showLocationDialog = true
        )
    }
    
    fun showLocationDialog(latLng: LatLng) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = latLng,
            showLocationDialog = true
        )
    }
    
    fun onMapBoundsChanged(
        minLat: Double, maxLat: Double, minLng: Double, maxLng: Double
    ) {
        viewModelScope.launch {
            try {
                getLocationsInBoundsUseCase(minLat, maxLat, minLng, maxLng).collect { locations ->
                    _uiState.value = _uiState.value.copy(
                        visibleLocations = locations
                    )
                }
            } catch (e: Exception) {
                // 忽略边界查询错误，不影响用户体验
            }
        }
    }
    
    fun searchLocations(query: String) {
        viewModelScope.launch {
            try {
                val results = searchLocationsUseCase(query)
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isSearching = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "搜索失败: ${e.message}",
                    isSearching = false
                )
            }
        }
    }
    
    // 导航功能
    fun startNavigation(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ) {
        aMapManager.startNavigation(startLatitude, startLongitude, endLatitude, endLongitude)
        _uiState.value = _uiState.value.copy(
            successMessage = "导航路线已规划"
        )
    }
    
    fun startNavigationToLocation(location: LocationTag) {
        _uiState.value.currentLocation?.let { current ->
            startNavigation(
                current.latitude,
                current.longitude,
                location.latitude,
                location.longitude
            )
        } ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "无法获取当前位置，请先定位"
            )
        }
    }
    
    fun clearNavigationMarkers() {
        aMapManager.clearNavigationMarkers()
        _uiState.value = _uiState.value.copy(
            successMessage = "导航标记已清除"
        )
    }
    
    // UI状态管理
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    fun hideLocationDialog() {
        _uiState.value = _uiState.value.copy(
            showLocationDialog = false,
            selectedLocation = null
        )
    }
    
    fun showTagDialog() {
        _uiState.value = _uiState.value.copy(
            showTagDialog = true
        )
    }
    
    fun hideTagDialog() {
        _uiState.value = _uiState.value.copy(
            showTagDialog = false
        )
    }
    
    fun showErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message
        )
    }
    
    fun showSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            successMessage = message
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        aMapLocationManager.destroy()
        aMapManager.onDestroy()
    }
}

data class MapUiState(
    val isLoading: Boolean = true,
    val isMapLoaded: Boolean = false,
    val isLocating: Boolean = false,
    val isSearching: Boolean = false,
    val tags: List<MapTag> = emptyList(),
    val locations: List<LocationTag> = emptyList(),
    val visibleLocations: List<LocationTag> = emptyList(),
    val searchResults: List<LocationTag> = emptyList(),
    val currentLocation: LatLng? = null,
    val currentAddress: String? = null,
    val selectedLocation: LatLng? = null,
    val selectedTagId: Long? = null,
    val showLocationDialog: Boolean = false,
    val showTagDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)