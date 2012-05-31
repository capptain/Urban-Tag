"use strict";

function Map(_manager, name)
{
    this.standardProj = new OpenLayers.Projection("EPSG:4326");
    this.googleProj = new OpenLayers.Projection("EPSG:900913");
    this.manager = _manager;
    this.shapes = new Array();
    this.osmMap = new OpenLayers.Map(name);
    this.mapLayer = new OpenLayers.Layer.OSM();
    this.placeLayer = new OpenLayers.Layer.Vector("places");
    this.osmMap.addLayer(this.mapLayer);
    this.osmMap.addLayer(this.placeLayer);
    
    this.placeSelectedRegistration = this.manager.addPlaceSelectedListener(this.onPlaceSelected.bind(this));
    this.placeAddedRegistration = this.manager.addPlaceAddedListener(this.onPlaceAdded.bind(this));
    
    for(var i = 0; i < this.manager.places.length; i++)
    {
        this.drawPlace(this.manager.places[i]);
    }
}

Map.prototype.goTo = function(longitude, latitude, zoom)
{
    var currentCenter = this.osmMap.center;
    var longitudeDiff = currentCenter.lon - longitude;
    
    this.osmMap.setCenter(longitude, latitude);
};

Map.prototype.onPlaceSelected = function(place)
{
    var circle = this.shapes[place.name];
    var bounds = circle.bounds;
    this.osmMap.zoomToExtent(bounds, false);
};

Map.prototype.onPlaceAdded = function(place)
{
  // Draw the place
  var circle = this.drawPlace(place);
  
  // Add the circle to datastructure
  this.shapes[place.name] = circle;
};

Map.prototype.drawPlace = function(place)
{
    // Construct the circle points
    var center = new OpenLayers.LonLat(place.longitude, place.latitude);
    var radius = place.radius;
    center.transform(this.standardProj, this.googleProj);
    
    var points = [];
    var point;
    var i;
    for(i = 0; i < 99; ++i)
    {
        var a = i * (2 * Math.PI) / 100;
        var x = center.lon + (radius * Math.cos(a));
        var y = center.lat + (radius * Math.sin(a));
        var position = new OpenLayers.LonLat(x, y);
        point = new OpenLayers.Geometry.Point(position.lon, position.lat);
        
        points.push(point);
    }
    points.push(new OpenLayers.Geometry.Point(points[0].x, points[0].y));
    
    // Construct the circle from points
    var linearRing = new OpenLayers.Geometry.LinearRing(points);
    var polygon = new OpenLayers.Geometry.Polygon([linearRing]);
    var circle = new OpenLayers.Feature.Vector(polygon);
    
    // Draw the circle
    this.placeLayer.addFeatures(circle);
    this.placeLayer.redraw();
    
    return circle;
};