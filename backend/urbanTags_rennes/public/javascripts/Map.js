    // "use strict";

/**
* This class represents the object asociated to the map of the homepage (must be merged with MapBox soon)
*
* @class Map
* @constructor
* @param {Object} placeManager Place manager which contains place objects informations.
* @param {String} name Id of the map's container.
*/
function Map(placeManager, name)
{
    // Constants
    this.LONLAT_ANIMATION_DURATION = 500;
    this.LONLAT_ANIMATION_INTERVAL = 10;
    this.LONLAT_ANIMATION_STEP_NUMBER = this.LONLAT_ANIMATION_DURATION / this.LONLAT_ANIMATION_INTERVAL;
    this.ZOOM_DURATION = 500;
    
    // Link the map to the place manager
    this.placeManager = placeManager;
    this.name = name;
    
    // Data structures
    this.shapes = {};
    this.places = {};
    
    // Initialize map
    this.standardProj = new OpenLayers.Projection("EPSG:4326");
    this.googleProj = new OpenLayers.Projection("EPSG:900913");
    this.osmMap = new OpenLayers.Map(name);
    this.mapLayer = new OpenLayers.Layer.OSM();
    this.placeLayer = new OpenLayers.Layer.Vector("places", {
        rendererOptions: {zIndexing: true}
    });
    this.osmMap.addLayer(this.mapLayer);
    this.osmMap.addLayer(this.placeLayer);
    
    // Manage select feature for shape selection
    this.selectFeature = new OpenLayers.Control.SelectFeature(this.placeLayer);
    this.selectFeature.onUnselect = this.unselectFeatureHandler.bind(this);
    
    // Handler when selecting a shape using a mouse click
    this.selectFeature.onSelect = this.selectFeatureHandler.bind(this);
    this.osmMap.addControl(this.selectFeature);
    this.selectFeature.activate();
    
    // Register the Map to PlaceManager's events
    this.placeSelectedRegistration = this.placeManager.register("placeSelected", this.onPlaceSelected.bind(this));
    this.placeUnselectedRegistration = this.placeManager.register("placeUnselected", this.onPlaceUnselected.bind(this));
    this.placeAddedRegistration = this.placeManager.register("placeAdded", this.onPlaceAdded.bind(this));
    this.placeEditedRegistration = this.placeManager.register("placeEdited", this.onPlaceEdited.bind(this));
    this.placeDeletedRegistration = this.placeManager.register("placeDeleted", this.onPlaceDeleted.bind(this));
    
    // Draw current places
    for(var i = 0; i < this.placeManager.places.length; i++)
    {
        this.drawPlace(this.placeManager.places[i]);
    }
    
    $("#homepage-geocoding-button").bind('click', function()
        {
        this.geocode($("#homepage-geocoding-field").val());
        }.bind(this)
    );
    
    $("#homepage-geocoding-field").bind('focus', function()
    {
        $("#homepage-geocoding-alert").hide();
        if($("#homepage-geocoding-field").val() == "Rechercher un lieu")
        {
            $("#homepage-geocoding-field").val("");
            $("#homepage-geocoding-field").removeClass("hint");
        }
    }
    );
    
    $("#homepage-geocoding-field").bind('blur', function()
    {
        var value = $("#homepage-geocoding-field").val();
        if(value === "")
        {
            $("#homepage-geocoding-field").val("Rechercher un lieu");
            $("#homepage-geocoding-field").addClass("hint");
        }
    }.bind(this)
    );

    $("#homepage-geocoding-field").bind('change', function()
    {
        this.geocode($("#homepage-geocoding-field").val());
    }.bind(this));
    
    $("#homepage-geocoding-loader").hide();
    $("#homepage-geocoding-alert").hide();
}

/**
* This method makes the map centering on a specific longitude, latitude with a specific zoom level.
*
* @method goTo
* @param {Object} lonLat OpenLayers.LonLat object corresponding to the new map's center.
* @param {Integer} zoom New zoom level.
*/
Map.prototype.goTo = function(lonLat, zoom)
{
    var currentCenter = this.osmMap.center;
    var longitudeDiff = lonLat.lon - currentCenter.lon;
    var latitudeDiff = lonLat.lat - currentCenter.lat;
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

/**
* Handler when a vector feature (here a circle) is unselected.
*
* @method unselectFeatureHandler
* @param {Object} feature Unselected circle.
*/
Map.prototype.unselectFeatureHandler = function(feature)
{
    // Change style
    this.applyDefaultStyle(feature);
    
    // Notify the manager that it has to unselect the current
    this.placeManager.unselectPlace();
};

/**
* Handler when a vector feature (here a circle) is selected.
*
* @method selectFeatureHandler
* @param {Object} feature Selected circle.
*/
Map.prototype.selectFeatureHandler = function(feature)
{
    // Change style
    this.applyDefaultStyle(feature);
    
    // Get the place
    var place = this.places[feature.id];
    
    // Notify the manager that the place has been selected
    this.placeManager.selectPlace(place);
};

/**
* Handles PlaceSelected event. select the shape corresponding to the place and centers the map on it.
*
* @method onPlaceSelected
* @param {Object} place Selected place object
*/
Map.prototype.onPlaceSelected = function(place)
{
    // Retrieve the shape corresponding to the selected place
    var circle = this.shapes[place.id];
    var bounds = circle.geometry.bounds;
    
    // Disable unselect handler which dispatch event to the manager
    this.selectFeature.onUnselect = this.applyDefaultStyle.bind(this);
    
    // Unselect previous selected shape
    this.selectFeature.unselectAll();
    
    // Enable unselect handler which dispatch event to the manager
    this.selectFeature.onUnselect = this.unselectFeatureHandler.bind(this);
    
    // Disable old select feature's handler to avoid infinite loop with the place manager
    this.selectFeature.onSelect = this.applySelectStyle.bind(this);
    
    // Select the shape corresponding to the new selected place
    this.selectFeature.select(circle);
    
    // Enable the select feature's handler
    this.selectFeature.onSelect = this.selectFeatureHandler.bind(this);
    
    // Center the map on the shape
    var zoom = this.osmMap.getZoomForExtent(bounds, false);
    this.goTo(bounds.getCenterLonLat(), zoom);
};

/**
* Handles a PlaceUnselected event. Grows the map container.
*
* @method onPlaceUnselected
* @param {Object} place Unselected place object.
*/
Map.prototype.onPlaceUnselected = function(place)
{
    focusOnTop();
};

/**
* Handles PlaceAdded event. Draw the shape corresponding to the selected place and push datastructures.
*
* @method onPlaceAdded
* @param {Object} place Added place object.
*/
Map.prototype.onPlaceAdded = function(place)
{
  // Draw the place
  var circle = this.drawPlace(place);
  
  // Add the circle to datastructure
  this.shapes[place.id] = circle;
  this.places[circle.id] = place;
};

/**
* Handles PlaceEditedEvent. Remove the circle corresponding to the old place information. Draw the circle corresponding to the new data
*
* @method onPlaceEdited
* @param {Object} place Edited place object.
*/
Map.prototype.onPlaceEdited = function(place)
{
    var circle = this.shapes[place.id];
    this.placeLayer.removeFeatures([circle]);
    
    circle = this.drawPlace(place);
    this.shapes[place.id] = circle;
    this.places[circle.id] = place;
    
    this.onPlaceSelected(place);
};

/**
* Handles PlaceDeletedEvent. Remove the circle corresponding to the deleted place.
*
* @method onPlaceDeleted
* @param {Object} place Deleted place object.
*/
Map.prototype.onPlaceDeleted = function(place)
{
	var circle = this.shapes[place.id];
	this.placeLayer.removeFeatures([circle]);
	
	delete this.places[circle.id];
	delete this.shapes[place.id];
};

/**
 * Draw a place as a circle and return the related Polygon.
 *
 * @method drawPlace
 * @param {Object} place Place object which must be drawn.
 */
Map.prototype.drawPlace = function(place)
{
    // Get center and radius in variables
    var center = new OpenLayers.LonLat(place.longitude, place.latitude);
    var radius = place.radius;
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
    
    // Set correct style
    var style = new OpenLayers.Style();
    circle.style = style;
    style.fillColor = "#"+place.mainTag.color;
    style.strokeColor = "#"+place.mainTag.color;
    style.fillOpacity = 0.3;
    style.strokeOpacity = 0.5;
    
    // Add the circle to the map layer
    this.placeLayer.addFeatures(circle);
    
    // Refresh the map layer
    this.placeLayer.redraw();
    
    // Return the circle
    return circle;
};

/**
* Apply a selected style to a circle.
*
* @method applySelectStyle
* @param {Object} feature Circle concerned by the selected style
*/
Map.prototype.applySelectStyle = function(feature)
{
    var style = feature.style;
    if(style === null)
    {
        style = new OpenLayers.Style();
        feature.style = style;
    }
    style.fillOpacity = 0.8;
    style.strokeOpacity = 1;
    this.placeLayer.redraw();
};

/**
* Apply the blur style to a circle.
*
* @method applyDefaultStyle
* @param {Object} feature Circle concerned by the blur style
*/
Map.prototype.applyDefaultStyle = function(feature)
{
    var style = feature.style;
    if(style === null)
    {
        style = new OpenLayers.Style();
        feature.style = style;
    }
    style.fillOpacity = 0.3;
    style.strokeOpacity = 0.5;
    this.placeLayer.redraw();
};

/**
* Look for a place
*
* @method geocode
* @param {String} query Place name.
*/
Map.prototype.geocode = function(query)
{
    $("#homepage-geocoding-loader").show();
    $.get(jsRoutes.geocoding.geocode({"query": query}), function(data)
    {
        $("#homepage-geocoding-loader").hide();
        if(data.length > 0)
        {
            var position = data[0];
            var lonlat = new OpenLayers.LonLat(position.lon, position.lat);
            lonlat.transform(this.standardProj, this.googleProj);
            var bounds = new OpenLayers.Bounds(position.boundingbox[0], position.boundingbox[2], position.boundingbox[1], position.boundingbox[3]);
            bounds.transform(this.standardProj, this.googleProj);
            var zoom = this.osmMap.getZoomForExtent(bounds, false);
            
            this.goTo(lonlat, zoom);
        }
        else
        {
            $("#homepage-geocoding-alert").show();
        }
    }.bind(this)).error(
    function(data)
    {
        console.log(data);
    });
};