function PlaceWizard(_placeManager, _place)
{
    this.placeManager = _placeManager;
    this.mapBox = null;
    this.thirdStepMap = null;
    
    if(typeof _place != "undefined")
    {
        this.data.id = _place.id;
        this.data.name = _place.name;
        this.data.accuracy = _place.accuracy;
        this.data.expiration = _place.expiration;
        
        this.data.tags = [];
        for(var i = 0; i < _place.tags.length; i++)
        {
            this.data.tags[i] = _place.tags[i].id;
        }
        this.data.mainTag = _place.mainTag.id;
        this.data.longitude = _place.longitude;
        this.data.latitude = _place.latitude;
        this.data.radius = _place.radius;
        
        getMessage("place.wizard.title.edit", function(data){ $("#place-wizard-title").html(data) });
    }
    else
    {
        this.data = {};
        getMessage("place.wizard.title.add", function(data){ $("#place-wizard-title").html(data) });
    }
    
    //Views
    this.templateView = null;
    $.get(jsRoutes.placeWizardRoutes.getTemplate(), function(data)
    {
        this.templateView = data;
    }.bind(this));
    
    this.firstStepView = null;
    $.get(jsRoutes.placeWizardRoutes.firstStep(), function(data)
    {
        this.firstStepView = data;
    }.bind(this));
    
    this.secondStepView = null;
    $.get(jsRoutes.placeWizardRoutes.secondStep(), function(data)
    {
        this.secondStepView = data;
    }.bind(this));
    
    this.thirdStepView = null;
    $.get(jsRoutes.placeWizardRoutes.thirdStep(), function(data)
    {
        this.thirdStepView = data;
    }.bind(this));
    this.thirdStepAlert = null;
    
    this.templateHtml = null;
    this.firstStepHtml = null;
    this.secondStepHtml = null;
}


/*
 * FIRST STEP
 */

PlaceWizard.prototype.displayFirstStep = function()
{
    if(this.firstStepView == null)
    {
        $.get(jsRoutes.placeWizardRoutes.firstStep(), function(data)
        {
            this.firstStepView = data;
            this.displayFirstStep();
        }.bind(this));
        
        return;
    }
    
    if(this.firstStepHtml == null)
    {
        this.firstStepHtml = $(this.firstStepView);
    }
    
    $(".custom-breadcrumb li.first", this.templateHtml).addClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).removeClass("current");
    
    
    $("#place-wizard-step-content").html(this.firstStepHtml);
    
    if(typeof this.data.name != "undefined")
    {
        $("#place-wizard-first-step-name-text", this.firstStepHtml).val(this.data.name);
    }
    
    if(typeof this.data.accuracy != "undefined")
    {
        $("#place-wizard-first-step-accuracy", this.firstStepHtml).val(this.data.accuracy);
    }
    
    if(typeof this.data.expiration != "undefined")
    {
        $("#place-wizard-first-step-expiration", this.firstStepHtml).val(this.data.expiration);
    }
    
    if(typeof this.data.tags != "undefined")
    {
        for(var i = 0; i < this.data.tags.length; i++)
        {
            var tagId = this.data.tags[i];
            var elt = $("#place-wizard-first-step-tags-select option[value='"+tagId+"']");
            elt.attr("selected", "selected");
        }
    }
    
    if(typeof this.data.mainTag != "undefined")
    {
        var tagId = this.data.mainTag;
        var elt = $("#place-wizard-first-step-mainTag-select option[value='"+tagId+"']"); 
        elt.attr("selected", "selected");
    }
    
    $("#place-wizard-first-step-name-text-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-accuracy-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-expiration-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-tags-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).hide();
    
    this.initFirstStepHandlers();
};

PlaceWizard.prototype.initFirstStepHandlers = function()
{
    // Set button handlers
    $("#place-wizard-first-step-cancel-btn").bind('click', function(){ this.close(); }.bind(this) );
    $("#place-wizard-first-step-next-btn").bind('click', function(){ this.validateFirstStepForm(); }.bind(this) );
};

PlaceWizard.prototype.validateFirstStepForm = function()
{
    var name = $("#place-wizard-first-step-name-text").val();
    var accuracy = $("#place-wizard-first-step-accuracy").val();
    var expiration = $("#place-wizard-first-step-expiration").val();
    var tags = $("#place-wizard-first-step-tags-select").val();
    var mainTag = $("#place-wizard-first-step-mainTag-select").val();

    $("#place-wizard-first-step-name-text-errorMsg").slideUp(300);
    $("#place-wizard-first-step-accuracy-errorMsg").slideUp(300);
    $("#place-wizard-first-step-expiration-errorMsg").slideUp(300);
    $("#place-wizard-first-step-tags-errorMsg").slideUp(300);
    $("#place-wizard-first-step-mainTag-errorMsg").slideUp(300);
    
    var id = (typeof this.data.id != "undefined") ? this.data.id : -1;
    $.post(jsRoutes.placeWizardRoutes.validation.firstStep({"id": id, "name": name, "accuracy": accuracy, "expiration": expiration, "tags": tags, "mainTag": mainTag}), function(errors)
    {
        // If everything is ok, go to the second step
    	this.data.name = name;
    	this.data.accuracy = accuracy;
    	this.data.expiration = expiration;
    	this.data.tags = tags;
    	this.data.mainTag = mainTag;
    	
        this.displaySecondStep();
    }.bind(this)).error(function(data)
	{
    	var errors = jQuery.parseJSON(data.responseText);
    	
    	if(typeof errors["name"] != "undefined")
        {
            $("#place-wizard-first-step-name-text-errorMsg").slideDown(300);
            $("#place-wizard-first-step-name-text-errorMsg").html(errors["name"]);
        }
        if(typeof errors["accuracy"] != "undefined")
        {
            $("#place-wizard-first-step-accuracy-errorMsg").slideDown(300);
            $("#place-wizard-first-step-accuracy-errorMsg").html(errors["accuracy"]);
        }
        if(typeof errors["expiration"] != "undefined")
        {
            $("#place-wizard-first-step-expiration-errorMsg").slideDown(300);
            $("#place-wizard-first-step-expiration-errorMsg").html(errors["expiration"]);
        }
        if(typeof errors["tags"] != "undefined")
        {
            $("#place-wizard-first-step-tags-errorMsg").slideDown(300);
            $("#place-wizard-first-step-tags-errorMsg").html(errors["tags"]);
        }
        if(typeof errors["mainTag"] != "undefined")
        {
            $("#place-wizard-first-step-mainTag-errorMsg").slideDown(300);
            $("#place-wizard-first-step-mainTag-errorMsg").html(errors["mainTag"]);
        }
	}.bind(this));
};




/*
 * SECOND STEP
 */

PlaceWizard.prototype.displaySecondStep = function()
{
    if(this.secondStepView == null)
    {
        $.get(jsRoutes.placeWizardRoutes.secondStep(), function(data)
        {
            this.secondStepView = data;
            this.displaySecondStep();
        }.bind(this));
        
        return;
    }
    
    if(this.secondStepHtml == null)
    {
        this.secondStepHtml = $(this.secondStepView);
    }
    
    $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).addClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).removeClass("current");
    
    $("#place-wizard-step-content").html(this.secondStepHtml);
    
    $("#place-wizard-second-step-longitude-errorMsg", this.secondStepHtml).hide();
    $("#place-wizard-second-step-latitude-errorMsg", this.secondStepHtml).hide();
    $("#place-wizard-second-step-radius-errorMsg", this.secondStepHtml).hide();
    
    if(this.mapBox == null)
	{
        var options = {
          onFeatureDrawn: function(feature, fireEvent)
          {
              // Remove old feature
              this.drawLayer.removeAllFeatures();
              this.drawLayer.addFeatures([feature]);
              this.shape = feature;
              
              if(typeof fireEvent == "undefined" || fireEvent == true)
              {
                  this.fireEvent("shapeModified", this.shape);
              }
          }  
        };
        
        if(typeof this.data.longitude != "undefined" && typeof this.data.latitude != "undefined" && typeof this.data.radius != "undefined")
        {
            this.mapBox = new MapBox("place-wizard-second-step-map-container", options, new OpenLayers.LonLat(this.data.longitude, this.data.latitude));
            this.mapBox.drawCircle(this.data.longitude, this.data.latitude, this.data.radius);
            
            $("#place-wizard-second-step-longitude").val(this.data.longitude);
            $("#place-wizard-second-step-latitude").val(this.data.latitude);
            $("#place-wizard-second-step-radius").val(this.data.radius);
        }
        else
        {
            this.mapBox = new MapBox("place-wizard-second-step-map-container", options);
        }
	}
    
	this.initSecondStepHandlers();
	$("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val(this.data.name + " Rennes");
};

PlaceWizard.prototype.initSecondStepHandlers = function()
{
    $("#place-wizard-second-step-previous-btn").bind('click', function(){
    	this.displayFirstStep();
	}.bind(this));
    
    $("#place-wizard-second-step-cancel-btn").bind('click', function()
	{
    	this.close();
	}.bind(this));
    
    $("#place-wizard-second-step-next-btn").bind('click', function()
		{
    		this.validateSecondStepForm();
		}.bind(this)
	);
    
    $("#place-wizard-second-step-geocoding-button", this.secondStepHtml).bind('click', function()
        {
        this.geocode($("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val());
        }.bind(this)
    );
    
    // Initialize button handlers
    this.navigateButton = $("#place-wizard-second-step-navigate-button");
    this.drawButton = $("#place-wizard-second-step-draw-button");
    this.transformButton = $("#place-wizard-second-step-transform-button");
    
    this.navigateButton.bind('click', this.onNavigateButtonClicked.bind(this));
    this.drawButton.bind('click', this.onDrawButtonClicked.bind(this));
    
    
    if(typeof this.shapeModifiedRegistration != "undefined")
	{
    	this.shapeModifiedRegistration.removeHandler();
	}
    this.shapeModifiedRegistration = this.mapBox.register("shapeModified", this.onShapeModified.bind(this));
    $("#place-wizard-second-step-longitude", this.secondStepHtml).bind('change', this.onLongitudeChanged.bind(this));
    $("#place-wizard-second-step-latitude", this.secondStepHtml).bind('change', this.onLatitudeChanged.bind(this));
    $("#place-wizard-second-step-radius", this.secondStepHtml).bind('change', this.onRadiusChanged.bind(this));
};

PlaceWizard.prototype.validateSecondStepForm = function()
{
	var longitude = $("#place-wizard-second-step-longitude").val();
	var latitude = $("#place-wizard-second-step-latitude").val();
	var radius = $("#place-wizard-second-step-radius").val();
	
	this.data.longitude = longitude;
	this.data.latitude = latitude;
	this.data.radius = radius;
	
	$("#place-wizard-second-step-longitude-errorMsg", this.secondStepHtml).slideUp(300);
	$("#place-wizard-second-step-latitude-errorMsg", this.secondStepHtml).slideUp(300);
	$("#place-wizard-second-step-radius-errorMsg", this.secondStepHtml).slideUp(300);
	
	$.post(jsRoutes.placeWizardRoutes.validation.secondStep({'longitude': longitude , 'latitude': latitude, 'radius': radius}), function(data)
	{
	    this.displayThirdStep();
	}.bind(this)
	).error(
		function(data)
		{
			var errors = jQuery.parseJSON(data.responseText);
			
			if(typeof errors.longitude != "undefined")
			{
				$("#place-wizard-second-step-longitude-errorMsg", this.secondStepHtml).html(errors.longitude);
				$("#place-wizard-second-step-longitude-errorMsg", this.secondStepHtml).slideDown(300);
			}
			
			if(typeof errors.latitude != "undefined")
			{
				$("#place-wizard-second-step-latitude-errorMsg", this.secondStepHtml).html(errors.latitude);
				$("#place-wizard-second-step-latitude-errorMsg", this.secondStepHtml).slideDown(300);
			}
			
			if(typeof errors.radius != "undefined")
			{
				$("#place-wizard-second-step-radius-errorMsg", this.secondStepHtml).html(errors.radius);
				$("#place-wizard-second-step-radius-errorMsg", this.secondStepHtml).slideDown(300);
			}
		}.bind(this)
	);
};

PlaceWizard.prototype.onShapeModified = function(feature)
{
	var bounds = feature.geometry.getBounds();
	var center = bounds.getCenterLonLat();
	var left = bounds.toArray()[0];
	var radius = Math.round(center.lon - left);
	
	var dupCenter = new OpenLayers.LonLat(center.lon, center.lat);
	dupCenter.transform(this.mapBox.googleProj, this.mapBox.standardProj);
	
	if(typeof $("#place-wizard-second-step-longitude", this.secondStepHtml).attr("disabled") != "undefined")
	{
		$("#place-wizard-second-step-longitude", this.secondStepHtml).removeAttr("disabled");
	    $("#place-wizard-second-step-latitude", this.secondStepHtml).removeAttr("disabled");
	    $("#place-wizard-second-step-radius", this.secondStepHtml).removeAttr("disabled");
	}
	
	$("#place-wizard-second-step-longitude", this.secondStepHtml).val(dupCenter.lon);
	$("#place-wizard-second-step-latitude", this.secondStepHtml).val(dupCenter.lat);
	$("#place-wizard-second-step-radius", this.secondStepHtml).val(radius);
	
	if(this.transformButton.hasClass('disabled'))
    {
        this.transformButton.removeClass('disabled');
        this.transformButton.bind('click', this.onTransformButtonClicked.bind(this));
    }
};

PlaceWizard.prototype.onNavigateButtonClicked = function()
{
    this.navigateButton.addClass("btn-primary");
    this.drawButton.removeClass("btn-primary");
    this.transformButton.removeClass("btn-primary");
    
    this.mapBox.navigationMode();
};

PlaceWizard.prototype.onDrawButtonClicked = function()
{
    this.navigateButton.removeClass("btn-primary");
    this.drawButton.addClass("btn-primary");
    this.transformButton.removeClass("btn-primary");
    
    this.mapBox.drawingMode();
};

PlaceWizard.prototype.onTransformButtonClicked = function()
{
    this.navigateButton.removeClass("btn-primary");
    this.drawButton.removeClass("btn-primary");
    this.transformButton.addClass("btn-primary");
    
    this.mapBox.editingMode();
};

PlaceWizard.prototype.onLongitudeChanged = function()
{
	var longitude = $("#place-wizard-second-step-longitude").val();
	if(isNumber(longitude))
	{
		this.mapBox.setShapeLongitude(longitude);
	}
};

PlaceWizard.prototype.onLatitudeChanged = function()
{
	var latitude = $("#place-wizard-second-step-latitude").val();
	if(isNumber(latitude))
	{
		this.mapBox.setShapeLatitude(latitude);
	}
};

PlaceWizard.prototype.onRadiusChanged = function()
{
	var radius = $("#place-wizard-second-step-radius").val();
	if(isNumber(radius) && radius > 0)
	{
		this.mapBox.setShapeRadius(radius);
	}
};

/*
 * THIRD STEP
 */
PlaceWizard.prototype.displayThirdStep = function()
{
    if(this.thirdStepView == null)
    {
        $.get(jsRoutes.placeWizardRoutes.thirdStep(), function(data)
        {
            this.thirdStepView = data;
            this.displayThirdStep();
        }.bind(this));
        
        return;
    }
    
    if(this.thirdStepHtml == null)
    {
        this.thirdStepHtml = $(this.thirdStepView);
    }
    
    $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).addClass("current");
    
    $("#place-wizard-step-content").html(this.thirdStepHtml);
    
    this.thirdStepAlert = $("#place-wizard-third-step-alert");
    this.thirdStepAlert.remove();
    
    
    $("#place-wizard-third-step-name").html(this.data.name);
    $("#place-wizard-third-step-accuracy").html(this.data.accuracy);
    $("#place-wizard-third-step-expiration").html(this.data.expiration);
    $("#place-wizard-third-step-maintag").html(this.data.mainTag);
    $("#place-wizard-third-step-longitude").html(this.data.longitude);
    $("#place-wizard-third-step-latitude").html(this.data.latitude);
    $("#place-wizard-third-step-radius").html(this.data.radius);
    
    $.post(jsRoutes.tag.getArray({'json': JSON.stringify(this.data.tags)}),
            function(data)
            {
                console.log(data);
                
                var content = "";
                for(var i=0; i < data.length; i++)
                {
                    var tag = data[i];
                    
                    if(tag.id == this.data.mainTag)
                    {
                        $("#place-wizard-third-step-mainTag-container").html("<span class='label' style='background: "+tag.color+";'>"+tag.name+"</span>");
                    }
                    
                    content += "<span class='label' style='background: "+tag.color+";'>"+tag.name+"</span> ";
                }
                $("#place-wizard-third-step-tags-container").html(content);
            }.bind(this)).error(
            function(data)
            {
                console.log("error : " + data);
            }
    )

    if(this.thirdStepMap != null)
    {
        this.thirdStepMap.destroy();
    }
    
    this.thirdStepMap = new OpenLayers.Map("place-wizard-third-step-map-container");
    var standardProj = new OpenLayers.Projection("EPSG:4326");
    var googleProj = new OpenLayers.Projection("EPSG:900913");
    
    var drawLayer = new OpenLayers.Layer.Vector("draw");
    
    // Get center and radius in variables
    var center = new OpenLayers.LonLat(this.data.longitude, this.data.latitude);
    center.transform(standardProj, googleProj);
    
    // Create circle's points
    var points = [];
    var point;
    var i;
    for(i = 0; i < 99; ++i)
    {
        var a = i * (2 * Math.PI) / 100;
        var x = center.lon + (this.data.radius * Math.cos(a));
        var y = center.lat + (this.data.radius * Math.sin(a));
        var position = new OpenLayers.LonLat(x, y);
        point = new OpenLayers.Geometry.Point(position.lon, position.lat);
        
        points.push(point);
    }
    points.push(new OpenLayers.Geometry.Point(points[0].x, points[0].y));
    
    // Construct the circle from points
    var linearRing = new OpenLayers.Geometry.LinearRing(points);
    var polygon = new OpenLayers.Geometry.Polygon([linearRing]);
    var circle = new OpenLayers.Feature.Vector(polygon);
    
    drawLayer.addFeatures([circle]);
    
    this.thirdStepMap.addLayer(new OpenLayers.Layer.OSM());
    this.thirdStepMap.addLayer(drawLayer);
    
    var zoom = this.thirdStepMap.getZoomForExtent(circle.geometry.getBounds(), false);
    
    this.thirdStepMap.setCenter(center, zoom);
    
    this.initThirdStepHandlers();
};

PlaceWizard.prototype.initThirdStepHandlers = function()
{
    $("#place-wizard-third-step-previous-btn").bind('click', function(){
        this.displaySecondStep();
    }.bind(this));
    
    $("#place-wizard-third-step-cancel-btn").bind('click', function()
    {
        this.close();
    }.bind(this));
    
    $("#place-wizard-third-step-next-btn").bind('click', function()
    {
        this.savePlace();
    }.bind(this));
};

PlaceWizard.prototype.savePlace = function()
{
  $.post(jsRoutes.place.add({'json': JSON.stringify(this.data)}),
      function(data)
      {
          if(typeof this.data.id == "undefined")
          {
              this.placeManager.addPlace(data);
          }
          else
          {
              this.placeManager.editPlace(data);
          }
          
          this.placeManager.selectPlace(data);
          this.close();
      }.bind(this)
    ).error(
      function(data)
      {
          $("#place-wizard-third-step-alert-container").html("<div class='alert alert-error'><button type='button' class='close' data-dismiss='alert'>x</button>Les données semblent incorrectes, nous vous conseillons de revenir à l'étape 1 pour les valider.</div>");
      }.bind(this)
    );
};


/*
 * POPUP METHODS
 */

PlaceWizard.prototype.show = function(_place)
{
    if(typeof _place != "undefined")
    {
        this.data.id = _place.id;
        this.data.name = _place.name;
        this.data.accuracy = _place.accuracy;
        this.data.expiration = _place.expiration;
        
        this.data.tags = [];
        for(var i = 0; i < _place.tags.length; i++)
        {
            this.data.tags[i] = _place.tags[i].id;
        }
        this.data.mainTag = _place.mainTag.id;
        this.data.longitude = _place.longitude;
        this.data.latitude = _place.latitude;
        this.data.radius = _place.radius;
        
        getMessage("place.wizard.title.edit", function(data){ $("#place-wizard-title").html(data) });
    }
    
    if(this.templateView == null)
    {
        $.get(jsRoutes.placeWizardRoutes.getTemplate(), function(data)
        {
            this.templateView = data;
            this.show(_place);
        }.bind(this));
        
        return;
    }
    
    if(this.templateHtml == null)
    {
        this.templateHtml = $(this.templateView);
    }
    $("body").append(this.templateHtml);
    $("#place-wizard-container").css("visibility", "visible");
    
//    this.initBreadCrumbHandlers();
    this.displayFirstStep();
    
    $("#place-wizard-content-container").center();
};

PlaceWizard.prototype.close = function()
{
    this.templateHtml.remove();
    this.data = {};
    
    this.templateHtml = null;
    this.firstStepHtml = null;
    this.secondStepHtml = null;
    this.thirdStepHtml = null;
    this.thirdStepMap = null;
    this.mapBox = null;
};

PlaceWizard.prototype.geocode = function(query)
{
	$.get(jsRoutes.geocoding.geocode({"query": query}), function(data)
	{
	    if(data.length > 0)
        {
	        var position = data[0];
	        var lonlat = new OpenLayers.LonLat(position.lon, position.lat);
	        var bounds = new OpenLayers.Bounds(position.boundingbox[0], position.boundingbox[2], position.boundingbox[1], position.boundingbox[3]);
	        bounds.transform(this.mapBox.standardProj, this.mapBox.googleProj);
	        var zoom = this.mapBox.osmMap.getZoomForExtent(bounds, false);
	        
	        this.mapBox.goTo(lonlat, zoom);
        }
	}.bind(this)).error(
	function(data)
	{
		console.log(data);
	});
};

PlaceWizard.prototype.initBreadCrumbHandlers = function()
{
    this.activateStep(1);
    this.activateStep(2);
    this.activateStep(3);
//    this.deactivateStep(2);
//    this.deactivateStep(3);
//    $(".custom-breadcrumb li.first", this.templateHtml).bind('click', function()
//    {
//        this.displayFirstStep();
//    }.bind(this));
//    
//    $(".custom-breadcrumb li.second", this.templateHtml).bind('click', function()
//    {
//        this.displaySecondStep();
//    }.bind(this));
//    
//    $(".custom-breadcrumb li.third", this.templateHtml).bind('click', function()
//    {
//        this.displayThirdStep();
//    }.bind(this));
};

PlaceWizard.prototype.activateStep = function(number)
{
    switch(number)
    {
        case 1:
            $(".custom-breadcrumb li.first", this.templateHtml).bind('click', function()
                    {
                        this.displayFirstStep();
                    }.bind(this));
            break;
            
        case 2:
            $(".custom-breadcrumb li.second", this.templateHtml).bind('click', function()
                    {
                        this.validateFirstStepForm();
                    }.bind(this));
            break;
            
        case 3:
            $(".custom-breadcrumb li.third", this.templateHtml).bind('click', function()
                    {
                        this.validateSecondStepForm();
                    }.bind(this));
    };
};

PlaceWizard.prototype.deactivateStep = function(number)
{
    switch(number)
    {
        case 1:
            $(".custom-breadcrumb li.first", this.templateHtml).unbind('click');
            break;
            
        case 2:
            $(".custom-breadcrumb li.second", this.templateHtml).unbind('click');
            break;
            
        case 3:
            $(".custom-breadcrumb li.third", this.templateHtml).unbind('click');
            break;
    };
};

