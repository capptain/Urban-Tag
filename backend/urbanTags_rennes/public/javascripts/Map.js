"use strict";

function Map(_manager, name)
{
    this.manager = _manager;
    this.osmMap = new OpenLayers.Map(name);
    
    this.placeSelectedRegistration = this.manager.addPlaceSelectedListener(this.onPlaceSelected.bind(this));
}

Map.prototype.goTo = function(longitude, latitude, zoom)
{
    this.osmMap.setCenter(longitude, latitude);
}

Map.prototype.onPlaceSelected = function(place)
{
    var bound = new OpenLayers.Bounds(place.longitude-0.1, place.latitude-0.1, place.longitude+0.1, place.latitude+0.1);
    var newCenter = new OpenLayers.LonLat(place.longitude, place.latitude);
    
    var standardProj = new OpenLayers.Projection("EPSG:4326");
    var googleProj = new OpenLayers.Projection("EPSG:900913");
    
    newCenter.transform(standardProj, googleProj);
    
    this.osmMap.setCenter(newCenter);
    this.osmMap.zoomTo(8);
    //this.osmMap.zoomToExtent(bound, false);
}