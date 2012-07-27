    // "use strict";

/**
* This class corresponds to the map of the place wizard where user draw the circle of the created/edited place.
*
* @class MapBox
* @constructor
* @param {String} containerId ID of the DOM element which is the container of the map.
* @param {Object} options Options of the constructor
* @param {Object} _lonlat Initial map center
* @param {Object} _zoom Initial map zoom level
*/
function MapBox(containerId, options, _lonlat, _zoom)
{
    CanFireEvents.call(this, ["shapeModified"]);
    
    // Constants
    this.LONLAT_ANIMATION_DURATION = 500;
    this.LONLAT_ANIMATION_INTERVAL = 10;
    this.LONLAT_ANIMATION_STEP_NUMBER = this.LONLAT_ANIMATION_DURATION / this.LONLAT_ANIMATION_INTERVAL;
    this.ZOOM_DURATION = 500;
    
    // Initialize map
    this.standardProj = new OpenLayers.Projection("EPSG:4326");
    this.googleProj = new OpenLayers.Projection("EPSG:900913");
    this.osmMap = new OpenLayers.Map(containerId);
    this.mapLayer = new OpenLayers.Layer.OSM();
    this.drawLayer = new OpenLayers.Layer.Vector("draw");
    this.shape = null;
    
    var drawOptions = {
            handlerOptions: {
                sides: 100,
                irregular: false
            }
    };
    
    if(typeof options != "undefined" && typeof options.onFeatureDrawn != "undefined")
    {
        drawOptions.featureAdded = options.onFeatureDrawn.bind(this);
    }
    
    // Initialize features
    this.drawFeature = new OpenLayers.Control.DrawFeature(this.drawLayer, OpenLayers.Handler.RegularPolygon, drawOptions);
    
    this.transformFeature = new OpenLayers.Control.TransformFeature(this.drawLayer,
    {
        rotate: false,
        ratio: 1,
        preserveAspectRatio: true
    });
    this.transformFeature.events.register("transform", this, function(feature){
            this.fireEvent("shapeModified", this.shape);
        }
    );
    
    // Add features
    this.osmMap.addControl(this.drawFeature);
    this.osmMap.addControl(this.transformFeature);
    
    var lonlat;
    if(typeof _lonlat == "undefined")
    {
        lonlat = new OpenLayers.LonLat(-1.675708, 48.113475);
    }
    else
    {
        lonlat = _lonlat;
    }
    lonlat.transform(this.standardProj, this.googleProj);
    
    var zoom = 13;
    if(typeof _zoom != "undefined")
    {
        zoom = _zoom;
    }
    
    this.osmMap.addLayer(this.mapLayer);
    this.osmMap.addLayer(this.drawLayer);
    
    this.osmMap.setCenter(lonlat);
    this.osmMap.zoomTo(zoom);
}

extend(MapBox.prototype, CanFireEvents.prototype);

/**
* Draw a circle on the map, with the given longitude, latitude and radius. Then center the map on it.
*
* @method drawCircle
* @param {Long} longitude Longitude of the circle's center.
* @param {Long} latitude Latitude of the circle's center.
* @param {Integer} radius Radius of the circle.
*/
MapBox.prototype.drawCircle = function(longitude, latitude, radius)
{
    // Get center and radius in variables
    var center = new OpenLayers.LonLat(longitude, latitude);
    center.transform(this.standardProj, this.googleProj);
    
    // Create circle's points
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
    
    this.drawLayer.addFeatures([circle]);
    
    var zoom = this.osmMap.getZoomForExtent(circle.geometry.getBounds(), false);
    this.goTo(center, zoom);
    
    if(typeof this.drawFeature.featureAdded != "undefined")
    {
        this.drawFeature.featureAdded(circle);
    }
};

/**
* Set the longitude of the circle, then center the map on it.
*
* @method setShapeLongitude
* @param {Long} longitude New longitude of the circle center.
*/
MapBox.prototype.setShapeLongitude = function(longitude)
{
    var bounds = this.shape.geometry.getBounds();
    var center = bounds.getCenterLonLat();
    var left = bounds.toArray()[0];
    var radius = Math.round(center.lon - left);
    var dupCenter = new OpenLayers.LonLat(center.lon, center.lat);
    dupCenter.transform(this.googleProj, this.standardProj);
    var latitude = dupCenter.lat;
    
    this.drawCircle(longitude, latitude, radius);
};

/**
* Set the latitude of the circle, then center the map on it.
*
* @method setShapeLatitude
* @param {Long} latitude New latitude of the circle center.
*/
MapBox.prototype.setShapeLatitude = function(latitude)
{
    var bounds = this.shape.geometry.getBounds();
    var center = bounds.getCenterLonLat();
    var left = bounds.toArray()[0];
    var radius = Math.round(center.lon - left);
    var dupCenter = new OpenLayers.LonLat(center.lon, center.lat);
    dupCenter.transform(this.googleProj, this.standardProj);
    var longitude = dupCenter.lon;
    
    this.drawCircle(longitude, latitude, radius);
};

/**
* Set the radius of the circle, then center the map on it.
*
* @method setShapeRadius
* @param {Integer} radius New radius of the circle.
*/
MapBox.prototype.setShapeRadius = function(radius)
{
    var bounds = this.shape.geometry.getBounds();
    var center = bounds.getCenterLonLat();
    var dupCenter = new OpenLayers.LonLat(center.lon, center.lat);
    dupCenter.transform(this.googleProj, this.standardProj);
    
    var longitude = dupCenter.lon;
    var latitude = dupCenter.lat;
    
    this.drawCircle(longitude, latitude, radius);
};


/**
* Handler executed after a circle has been dran on the map.
*
* @method onFeatureDrawn
* @param {Object} feature Drawn circle
* @param {Boolean} fireEvent Boolean indicating if the mapBox must fire an event indicating that a shape has been modified.
*/
MapBox.prototype.onFeatureDrawn = function(feature, fireEvent)
{
    // Remove old feature
    this.drawLayer.removeAllFeatures();
    this.drawLayer.addFeatures([feature]);
    this.shape = feature;
    
    if(typeof fireEvent == "undefined" || fireEvent === true)
    {
        this.fireEvent("shapeModified", this.shape);
    }
};

/**
* Put the MapBox in the navigation mode.
*
* @method navigationMode
*/
MapBox.prototype.navigationMode = function()
{
    this.drawFeature.deactivate();
    this.transformFeature.deactivate();
};

/**
* Put the MapBox in the drawing mode.
*
* @method drawingMode
*/
MapBox.prototype.drawingMode = function()
{
    this.drawFeature.activate();
    this.transformFeature.deactivate();
};

/**
* Put the MapBox in the edition mode.
*
* @method editingMode
*/
MapBox.prototype.editingMode = function()
{
    this.drawFeature.deactivate();
    this.transformFeature.activate();
    this.transformFeature.setFeature(this.shape);
};

/**
* Close the widget by removing the map.
*
* @method close
*/
MapBox.prototype.close = function()
{
    this.osmMap.remove();
};

/**
* Centers the map on the given position with the given zoom level.
*
* @method goTo
* @param {Object} lonLat OpenLayers.LonLat which is the new center position.
* @param {Integer} zoom New zoom level.
*/
MapBox.prototype.goTo = function(lonLat, zoom)
{
    lonLat.transform(this.standardProj, this.googleProj);
    
    var currentCenter = this.osmMap.center;
    var longitudeDiff = lonLat.lon - currentCenter.lon;
    var latitudeDiff = lonLat.lat - currentCenter.lat;
    
    if(typeof zoom == "undefined")
    {
        zoom = this.osmMap.zoom;
    }
    
    var zoomDiff = zoom - this.osmMap.zoom;
    var lonStep = longitudeDiff / this.LONLAT_ANIMATION_STEP_NUMBER;
    var latStep = latitudeDiff / this.LONLAT_ANIMATION_STEP_NUMBER;
    var cpt = 1;
    var obj = this;
    
    var runUnzoomAnimation = function(){
        var unzoomAnimation = setInterval(function()
        {
            if(zoomDiff >= 0 || obj.osmMap.getZoom() <= zoom)
            {
                clearInterval(unzoomAnimation);
                runMoveAnimation();
            }
            else
            {
                obj.osmMap.zoomOut();
            }
        }, (obj.ZOOM_DURATION/zoomDiff));
    };
    
    var runMoveAnimation = function(){
        var moveAnimation = setInterval(function()
        {
            var newCenter = new OpenLayers.LonLat(currentCenter.lon + (cpt*lonStep), currentCenter.lat + (cpt*latStep));
            obj.osmMap.setCenter(newCenter);
            cpt++;
            if(cpt > obj.LONLAT_ANIMATION_STEP_NUMBER)
            {
                clearInterval(moveAnimation);
                runZoomAnimation();
            }
        }, obj.LONLAT_ANIMATION_INTERVAL);
    };
    
    var runZoomAnimation = function(){
        var zoomAnimation = setInterval(function()
        {
            if(zoomDiff <= 0 || obj.osmMap.getZoom() >= zoom)
            {
                clearInterval(zoomAnimation);
            }
            else
            {
                obj.osmMap.zoomIn();
            }
        }, (obj.ZOOM_DURATION/zoomDiff));
    };
    
    runUnzoomAnimation();
};