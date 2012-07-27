    // "use strict";

/**
* This class represents the wizard used to add/edit a place
*
* @class PlaceWizard
* @constructor
* @param {Object} placeManager Object which containts places and manages them.
* @param {Object} place Optional place used if the wizard is used to edit a place.
*/
function PlaceWizard(placeManager, place)
{
    this.placeManager    = placeManager;
    this.mapBox          = null;
    this.thirdStepMap    = null;
    this.previousMainTag = null;
    this.isShown = false;
    
    if(typeof place != "undefined")
    {
        this.data.id         = place.id;
        this.data.name       = place.name;
        this.data.accuracy   = place.accuracy;
        
        this.data.tags = [];
        for(var i = 0; i < place.tags.length; i++)
            this.data.tags[i] = place.tags[i].id;

        this.data.mainTag   = place.mainTag.id;
        this.data.longitude = place.longitude;
        this.data.latitude  = place.latitude;
        this.data.radius    = place.radius;
        
        getMessage("place.wizard.title.edit", function(data){ $("#place-wizard-title").html(data); });
    }
    else
    {
        this.data = {};
        getMessage("place.wizard.title.add", function(data){ $("#place-wizard-title").html(data); });
    }
    
    //Views
    this.firstStepView = null;
    $.get(jsRoutes.placeWizardRoutes.firstStep(), function(data)
    {
        this.firstStepView = data;
    }.bind(this)).error(
        function(data){
            displayAlert("Impossible de récupérer la vue de la première étape auprès du serveur");
        }
    );
    
    this.secondStepView = null;
    $.get(jsRoutes.placeWizardRoutes.secondStep(), function(data)
    {
        this.secondStepView = data;
    }.bind(this)).error(
        function(data){
            displayAlert("Impossible de récupérer la vue de la deuxième étape auprès du serveur");
        }
    );
    
    this.thirdStepView = null;
    $.get(jsRoutes.placeWizardRoutes.thirdStep(), function(data)
    {
        this.thirdStepView = data;
    }.bind(this)).error(
        function(data){
            displayAlert("Impossible de récupérer la vue de la troisième étape auprès du serveur");
        }
    );
    
    this.templateView   = null;
    this.templateHtml   = null;
    this.firstStepHtml  = null;
    this.secondStepHtml = null;
    this.thirdStepHtml  = null;
}


/*
 * FIRST STEP
 */

/**
* This method displays the place basic informations step.
*
* @method displayFirstStep
*/
PlaceWizard.prototype.displayFirstStep = function()
{
    /* Get first step template if null */
    if(this.firstStepView === null)
    {
        $("#place-wizard-step-content").html("<div class='loader'></div>");
        showLoader($("#place-wizard-step-content"), this.templateHtml);
        $.get(jsRoutes.placeWizardRoutes.firstStep(), function(data)
        {
            hideLoader($("#place-wizard-step-content"), this.templateHtml);
            this.firstStepView = data;
            this.displayFirstStep();
        }.bind(this)).error(
            function(data)
            {
                displayAlert("Impossible de récupérer l'interface de l'étape auprès du serveur.", $("place-wizard-error-container", this.templateHtml));
            }
        );
        
        return;
    }
    
    if(this.firstStepHtml === null)
        this.firstStepHtml = $(this.firstStepView);

    $("#place-wizard-step-content").html(this.firstStepHtml);
    $(".custom-breadcrumb li.first", this.templateHtml).addClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).removeClass("current");
    
    if(typeof this.data.name != "undefined"){
        $("#place-wizard-first-step-name-text", this.firstStepHtml).val(this.data.name);
    }
    if(typeof this.data.accuracy != "undefined"){
        $("#place-wizard-first-step-accuracy", this.firstStepHtml).val(this.data.accuracy);
    }
    
    if(typeof this.data.tags != "undefined")
    {
        for(var i = 0; i < this.data.tags.length; i++)
        {
            var tagId  = this.data.tags[i];
            var tagElt = $("#place-wizard-first-step-tags-container a[data-value='"+tagId+"']");
            this.selectTag(tagElt);
        }
    }
    
    if(typeof this.data.mainTag != "undefined")
    {
        var obj = this;
        $("#place-wizard-first-step-mainTag-container a").each(function(index, object)
        {
           obj.unselectTag($(object));
        });
        
        var maintTagId  = this.data.mainTag;
        var maintTagElt = $("#place-wizard-first-step-mainTag-container a[data-value='"+maintTagId+"']");
        this.selectTag(maintTagElt);
        
        var newTagElement    = $("#place-wizard-first-step-tags-container a[data-value='"+maintTagId+"']");
        newTagElement.css("opacity", "0.4");
        this.selectTag(newTagElement);
        this.previousMainTag = maintTagId;
    }
    
    $("#place-wizard-first-step-name-text-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-accuracy-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-tags-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).hide();
    $("#place-wizard-first-step-unknown-errorMsg").hide();
    
    this.initFirstStepHandlers();
};

/**
* This method initializes handlers of the first step.
*
* @method initFirstStepHandlers
*/
PlaceWizard.prototype.initFirstStepHandlers = function()
{
    /* Remove old handlers */
    $("#place-wizard-previous-btn", this.templateHtml).unbind('click');
    $("#place-wizard-cancel-btn", this.templateHtml).unbind('click');
    $("#place-wizard-next-btn", this.templateHtml).unbind('click');
    $("#place-wizard-validate-btn", this.templateHtml).unbind('click');
    
    $("#place-wizard-previous-btn", this.templateHtml).hide();
    $("#place-wizard-validate-btn", this.templateHtml).hide();
    $("#place-wizard-next-btn", this.templateHtml).show();
    $("#place-wizard-cancel-btn", this.templateHtml).bind('click', function(){ this.close(); }.bind(this) );
    $("#place-wizard-next-btn", this.templateHtml).bind('click', function(){ this.validateFirstStepForm(); }.bind(this) );
    
    var obj = this;
    /* Main tag handlers */
    $("#place-wizard-first-step-mainTag-container a").bind('click', function()
    {
        $("#place-wizard-first-step-mainTag-container a").each(function(index, object)
        {
           obj.unselectTag($(object));
        });
        obj.onTagClicked($(this));
        if(obj.previousMainTag !== null)
        {
            var previousTagElement = $("#place-wizard-first-step-tags-container a[data-value='" + obj.previousMainTag + "']");
            previousTagElement.css("opacity", "1");
            obj.unselectTag(previousTagElement);
            
            previousTagElement.bind('click', function()
            {
                obj.onTagClicked($(this));
            });
        }
        
        var mainTagId     = $(this).attr("data-value");
        var newTagElement = $("#place-wizard-first-step-tags-container a[data-value='"+mainTagId+"']");
        newTagElement.css("opacity", "0.4");
        newTagElement.unbind('click');
        obj.selectTag(newTagElement);
        obj.previousMainTag = mainTagId;
    });
    
    /* Tag handlers */
    $("#place-wizard-first-step-tags-container a").bind('click', function()
    {
        obj.onTagClicked($(this));
    });
    $("#place-wizard-first-step-tags-container a[data-value='"+this.previousMainTag+"']").unbind('click');
};

/*
* Validate data of the first step's form. Go to the next step if ok.
*
* @method validateFirstStepForm
*/
PlaceWizard.prototype.validateFirstStepForm = function()
{
    showLoader($(".modal-footer", this.templateHtml));

    var postData      = {};
    postData.name     = $("#place-wizard-first-step-name-text").val();
    postData.accuracy = $("#place-wizard-first-step-accuracy").val();
    postData.tags     = [];
    $("#place-wizard-first-step-tags-container a").each(function(index, element)
    {
        if(typeof $(element).attr("selected") != "undefined") {
            postData.tags.push($(element).attr("data-value"));
        }
    });
    postData.mainTag = $("#place-wizard-first-step-mainTag-container a[selected='selected']", this.firstStepHtml).attr("data-value");

    $("#place-wizard-first-step-name-text-errorMsg").slideUp(300);
    $("#place-wizard-first-step-accuracy-errorMsg").slideUp(300);
    $("#place-wizard-first-step-tags-errorMsg").slideUp(300);
    $("#place-wizard-first-step-mainTag-errorMsg").slideUp(300);
    $("#place-wizard-first-step-unknown-errorMsg").slideUp(300);
    
    postData.id = (typeof this.data.id != "undefined") ? this.data.id : -1;
    $.post(jsRoutes.placeWizardRoutes.validation.firstStep({'json':JSON.stringify(postData)}), function(errors)
    {
        hideLoader($(".modal-footer", this.templateHtml));

        // If everything is ok, go to the second step
        this.data.name       = postData.name;
        this.data.accuracy   = postData.accuracy;
        this.data.tags       = postData.tags;
        this.data.mainTag    = postData.mainTag;
        
        this.displaySecondStep();
    }.bind(this)).error(function(data)
    {
        hideLoader($(".modal-footer", this.templateHtml));
        if(typeof data.status != "undefined" && data.status === 0)
        {
            displayAlert("Impossible de joindre le serveur pour vérifier vos données", $("#place-wizard-error-container"));
        }
        else if(typeof data.status != "undefined" && data.status === 400 && data.responseText !== "")
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
        }
        else
        {
            displayAlert("Oups, erreur inconnue.", $("#place-wizard-error-container"));
        }
    }.bind(this));
};


/*
 * SECOND STEP
 */

/**
* Display the step where user draw the circle corresponding to the POI of the place.
*
* @method displaySecondStep
*/
PlaceWizard.prototype.displaySecondStep = function()
{
    if(this.secondStepView === null)
    {
        $("#place-wizard-step-content").html("<div class='loader'></div>");
        showLoader($("#place-wizard-step-content"));
        $.get(jsRoutes.placeWizardRoutes.secondStep(), function(data)
        {
            hideLoader($("#place-wizard-step-content"));
            this.secondStepView = data;
            this.displaySecondStep();
        }.bind(this)).error(
            function(data)
            {
                displayAlert("Impossible de récupérer l'interface de l'étape auprès du serveur.", $("place-wizard-error-container", this.templateHtml));
            }
        );
        
        return;
    }
    
    if(this.secondStepHtml === null)
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
    
    if(this.mapBox === null)
    {
        var options = {
          onFeatureDrawn: function(feature, fireEvent)
          {
              // Remove old feature
              this.drawLayer.removeAllFeatures();
              this.drawLayer.addFeatures([feature]);
              this.shape = feature;
              
              if(typeof fireEvent == "undefined" || fireEvent === true)
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
    $("#place-wizard-second-step-geocoding-alert").hide();
};

/**
* Initialize handlers of the second step
*
* @method initSecondStepHandlers
*/
PlaceWizard.prototype.initSecondStepHandlers = function()
{
    /* Remove old handlers */
    $("#place-wizard-previous-btn", this.templateHtml).unbind('click');
    $("#place-wizard-cancel-btn", this.templateHtml).unbind('click');
    $("#place-wizard-next-btn", this.templateHtml).unbind('click');
    $("#place-wizard-validate-btn", this.templateHtml).unbind('click');
    
    $("#place-wizard-validate-btn", this.templateHtml).hide();
    $("#place-wizard-previous-btn", this.templateHtml).show();
    $("#place-wizard-next-btn", this.templateHtml).show();
    $("#place-wizard-previous-btn", this.templateHtml).bind('click', function(){
        this.displayFirstStep();
    }.bind(this));
    
    $("#place-wizard-cancel-btn", this.templateHtml).bind('click', function()
    {
        this.close();
    }.bind(this));
    
    $("#place-wizard-next-btn", this.templateHtml).bind('click', function()
        {
            this.validateSecondStepForm();
        }.bind(this)
    );
    
    $("#place-wizard-second-step-geocoding-button", this.secondStepHtml).bind('click', function() {
            this.geocode($("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val());
        }.bind(this)
    );
    
    $("#place-wizard-second-step-geocoding-field").bind('focus', function()
    {
        $("#place-wizard-second-step-geocoding-alert").hide();
        if($("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val() == "Rechercher une adresse")
        {
            $("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val("");
            $("#place-wizard-second-step-geocoding-field", this.secondStepHtml).removeClass("hint");
        }
    }
    );
    
    $("#place-wizard-second-step-geocoding-field").bind('blur', function()
    {
        var value = $("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val();
        if(value === "")
        {
            $("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val("Rechercher une adresse");
            $("#place-wizard-second-step-geocoding-field", this.secondStepHtml).addClass("hint");
        }
    }.bind(this)
    );

    $("#place-wizard-second-step-geocoding-field").bind('change', function()
    {
       this.geocode($("#place-wizard-second-step-geocoding-field", this.secondStepHtml).val());
    }.bind(this)
    );
    
    $("#place-wizard-second-step-geocoding-loader").hide();
    
    // Initialize button handlers
    this.navigateButton  = $("#place-wizard-second-step-navigate-button");
    this.drawButton      = $("#place-wizard-second-step-draw-button");
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

/**
* Validate data of the second step. Go to the next step if ok.
*
* @method validateSecondStepForm
*/
PlaceWizard.prototype.validateSecondStepForm = function()
{
    var longitude = $("#place-wizard-second-step-longitude").val();
    var latitude  = $("#place-wizard-second-step-latitude").val();
    var radius    = $("#place-wizard-second-step-radius").val();
    
    this.data.longitude = longitude;
    this.data.latitude  = latitude;
    this.data.radius    = radius;
    
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
            hideLoader($(".modal-footer", this.templateHtml));
            if(typeof data.status != "undefined" && data.status === 0)
            {
                displayAlert("Impossible de joindre le serveur pour vérifier vos données", $("#place-wizard-error-container"));
            }
            else if(typeof data.status != "undefined" && data.status === 400 && data.responseText !== "")
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
            }
            else
            {
                displayAlert("Oups, erreur inconnue.", $("#place-wizard-error-container"));
            }
        }.bind(this)
    );
};

/**
* Executed when a shape is modified on the map. Refresh forms.
*
* @method onShapeModified
* @param {Object} feature Modified circle.
*/
PlaceWizard.prototype.onShapeModified = function(feature)
{
    var bounds = feature.geometry.getBounds();
    var center = bounds.getCenterLonLat();
    var left   = bounds.toArray()[0];
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

/**
* Put the map box in the navigation mode.
*
* @method onNavigateButtonClicked
*/
PlaceWizard.prototype.onNavigateButtonClicked = function()
{
    this.navigateButton.addClass("btn-primary");
    this.drawButton.removeClass("btn-primary");
    this.transformButton.removeClass("btn-primary");
    
    this.mapBox.navigationMode();
};

/**
* Put the mapBox in the drawing mode.
*
* @method onDrawButtonClicked
*/
PlaceWizard.prototype.onDrawButtonClicked = function()
{
    this.navigateButton.removeClass("btn-primary");
    this.drawButton.addClass("btn-primary");
    this.transformButton.removeClass("btn-primary");
    
    this.mapBox.drawingMode();
};

/**
* Put the mapBox in the transform mode
*
* @method onTransformButtonClicked
*/
PlaceWizard.prototype.onTransformButtonClicked = function()
{
    this.navigateButton.removeClass("btn-primary");
    this.drawButton.removeClass("btn-primary");
    this.transformButton.addClass("btn-primary");
    
    this.mapBox.editingMode();
};

/**
* Executed when the longitude field value changes, redraw the circle.
*
* @method onLongitudeChanged
*/
PlaceWizard.prototype.onLongitudeChanged = function()
{
    var longitude = $("#place-wizard-second-step-longitude").val();
    if(isNumber(longitude))
    {
        this.mapBox.setShapeLongitude(longitude);
    }
};

/**
* Executed when the latitude field value changes, redraw the circle.
*
* @method onLatitudeChanged
*/
PlaceWizard.prototype.onLatitudeChanged = function()
{
    var latitude = $("#place-wizard-second-step-latitude").val();
    if(isNumber(latitude))
    {
        this.mapBox.setShapeLatitude(latitude);
    }
};

/**
* Executed when the radius field value changes, redraw the circle.
*
* @method onRadiusChanged
*/
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

/**
* Display the preview step.
*
* @method displayThirdStep
*/
PlaceWizard.prototype.displayThirdStep = function()
{
    if(this.thirdStepView === null)
    {
        $.get(jsRoutes.placeWizardRoutes.thirdStep(), function(data)
        {
            this.thirdStepView = data;
            this.displayThirdStep();
        }.bind(this)).error(
            function(data)
            {
                displayAlert("Impossible de récupérer l'interface de l'étape auprès du serveur.", $("place-wizard-error-container", this.templateHtml));
            }
        );
        
        return;
    }
    
    if(this.thirdStepHtml === null)
    {
        this.thirdStepHtml = $(this.thirdStepView);
    }
    
    $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).addClass("current");
    
    $("#place-wizard-step-content").html(this.thirdStepHtml);
    
    
    $("#place-wizard-third-step-name").html(this.data.name);
    $("#place-wizard-third-step-maintag").html(this.data.mainTag);
    $("#place-wizard-third-step-longitude").html(this.data.longitude);
    $("#place-wizard-third-step-latitude").html(this.data.latitude);
    $("#place-wizard-third-step-radius").html(this.data.radius);

    var accuracy;
    switch(this.data.accuracy)
    {
        case "high":
        accuracy = "Forte";
        break;

        case "medium":
        accuracy = "Moyenne";
        break;

        case "low":
        accuracy = "Faible";
        break;
    }
    $("#place-wizard-third-step-accuracy").html(accuracy);
    
    $.post(jsRoutes.tag.getArray({'json': JSON.stringify(this.data.tags)}),
        function(data)
        {
            console.log(data);
            
            var content = "";
            for(var i = 0; i < data.length; i++)
            {
                var tag = data[i];
                if(tag.id == this.data.mainTag) {
                    $("#place-wizard-third-step-mainTag-container").html("<span class='label' style='background:#"+tag.color+";'>"+tag.name+"</span>");
                }
                content += "<span class='label' style='background:#"+tag.color+";'>"+tag.name+"</span> ";
            }
            $("#place-wizard-third-step-tags-container").html(content);
        }.bind(this)).error(
        function(data)
        {
            displayAlert("Impossible de récupérer les tags auprès du serveur", $("place-wizard-error-container", this.templateHtml));
        }
        );

    if(this.thirdStepMap !== null)
    {
        this.thirdStepMap.destroy();
    }
    
    this.thirdStepMap = new OpenLayers.Map("place-wizard-third-step-map-container");
    var standardProj  = new OpenLayers.Projection("EPSG:4326");
    var googleProj    = new OpenLayers.Projection("EPSG:900913");
    var drawLayer     = new OpenLayers.Layer.Vector("draw");
    
    // Get center and radius in variables
    var center = new OpenLayers.LonLat(this.data.longitude, this.data.latitude);
    center.transform(standardProj, googleProj);
    
    // Create circle's points
    var points = [];
    var point;
    var i;
    for(i = 0; i < 99; ++i)
    {
        var a        = i * (2 * Math.PI) / 100;
        var x        = center.lon + (this.data.radius * Math.cos(a));
        var y        = center.lat + (this.data.radius * Math.sin(a));
        var position = new OpenLayers.LonLat(x, y);
        point        = new OpenLayers.Geometry.Point(position.lon, position.lat);
        
        points.push(point);
    }
    points.push(new OpenLayers.Geometry.Point(points[0].x, points[0].y));
    
    // Construct the circle from points
    var linearRing = new OpenLayers.Geometry.LinearRing(points);
    var polygon    = new OpenLayers.Geometry.Polygon([linearRing]);
    var circle     = new OpenLayers.Feature.Vector(polygon);
    
    drawLayer.addFeatures([circle]);
    
    this.thirdStepMap.addLayer(new OpenLayers.Layer.OSM());
    this.thirdStepMap.addLayer(drawLayer);
    
    var zoom = this.thirdStepMap.getZoomForExtent(circle.geometry.getBounds(), false);
    
    this.thirdStepMap.setCenter(center, zoom);
    this.initThirdStepHandlers();
};

/**
* Initialize preview step handlers
*
* @method initThirdStepHandlers
*/
PlaceWizard.prototype.initThirdStepHandlers = function()
{
    /* Remove old handlers */
    $("#place-wizard-previous-btn", this.templateHtml).unbind('click');
    $("#place-wizard-cancel-btn", this.templateHtml).unbind('click');
    $("#place-wizard-next-btn", this.templateHtml).unbind('click');
    $("#place-wizard-validate-btn", this.templateHtml).unbind('click');

    $("#place-wizard-next-btn", this.templateHtml).hide();
    $("#place-wizard-validate-btn", this.templateHtml).show();
    
    /* Add new handlers */
    $("#place-wizard-previous-btn", this.templateHtml).bind('click', function(){
        this.displaySecondStep();
    }.bind(this));
    
    $("#place-wizard-cancel-btn", this.templateHtml).bind('click', function()
    {
        this.close();
    }.bind(this));
    
    $("#place-wizard-validate-btn", this.templateHtml).bind('click', function()
    {
        this.savePlace();
    }.bind(this));
};

/**
* Save the place to the server. If the place already exists, edit it.
*
* @method savePlace
*/
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
        if(typeof data.status != "undefined" && data.status === 0)
        {
            displayAlert("La connexion avec le serveur a été perdue.", $("place-wizard-error-container", this.templateHtml));
        }
        else if(typeof data.status != "undefined" && data.status === 400)
        {
            displayAlert("Erreur lors de la sauvegarde, merci de vérifier vos données.", $("place-wizard-error-container", this.templateHtml));
        }
        else
        {
            displayAlert("Oups, erreur inconnue.", $("place-wizard-error-container", this.templateHtml));
        }
      }.bind(this)
    );
};

/**
* Show the view of the wizard in a popup container
*
* @method show
* @param {Object} place Optional place for edition mode.
*/
PlaceWizard.prototype.show = function(place)
{
    if(typeof place != "undefined")
    {
        this.data.id         = place.id;
        this.data.name       = place.name;
        this.data.accuracy   = place.accuracy;
        
        this.data.tags = [];
        for(var i = 0; i < place.tags.length; i++)
        {
            this.data.tags[i] = place.tags[i].id;
        }
        this.data.mainTag   = place.mainTag.id;
        this.data.longitude = place.longitude;
        this.data.latitude  = place.latitude;
        this.data.radius    = place.radius;
        
        getMessage("place.wizard.title.edit", function(data){ $("#place-wizard-title").html(data); });
    }
    
    if(this.templateView === null)
    {
        $.get(jsRoutes.placeWizardRoutes.getTemplate(), function(data)
        {
            this.templateView = data;
            this.show(place);
        }.bind(this)).error(function(data){
            displayAlert("Impossible de charger l'interface de l'assistant d'ajout de lieu.");
        });
        
        return;
    }
    
    if(this.templateHtml === null)
    {
        this.templateHtml = $(this.templateView);
        $("body").append(this.templateHtml);
    }
    
    this.displayFirstStep();
    this.templateHtml.modal({'show': true, 'backdrop': 'static'});
};

/**
* Close the wizard and reset its data
*
* @method close
*/
PlaceWizard.prototype.close = function()
{
    this.templateHtml.modal('hide');
    this.data           = {};
    this.firstStepHtml  = null;
    this.secondStepHtml = null;
    this.thirdStepHtml  = null;
    this.thirdStepMap   = null;
    this.mapBox         = null;
};

/**
* Look for a place with the given name
*
* @method geocode
* @param {String} query Place name
*/
PlaceWizard.prototype.geocode = function(query)
{
    $("#place-wizard-second-step-geocoding-loader").show();
    $.get(jsRoutes.geocoding.geocode({"query": query}), function(data)
    {
        $("#place-wizard-second-step-geocoding-loader").hide();
        if(data.length > 0)
        {
            $("#place-wizard-second-step-geocoding-alert").hide();
            var position = data[0];
            var lonlat   = new OpenLayers.LonLat(position.lon, position.lat);
            var bounds   = new OpenLayers.Bounds(position.boundingbox[0], position.boundingbox[2], position.boundingbox[1], position.boundingbox[3]);
            bounds.transform(this.mapBox.standardProj, this.mapBox.googleProj);
            var zoom     = this.mapBox.osmMap.getZoomForExtent(bounds, false);
            
            this.mapBox.goTo(lonlat, zoom);
        }
        else
        {
            $("#place-wizard-second-step-geocoding-alert").show();
        }
    }.bind(this)).error(
    function(data)
    {
        console.log(data);
    });
};

/**
* Initialize the breadcrumb handlers
*
* @method initBreadCrumbHandlers
*/
PlaceWizard.prototype.initBreadCrumbHandlers = function()
{
    this.activateStep(1);
    this.activateStep(2);
    this.activateStep(3);
};

/**
* Activate the given step in the breadcrumb
*
* @method activateStep
* @param {Integer} number Step number
*/
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
    }
};

/**
* Deactivate the given step in the breadcrumb
*
* @method deactivateStep
* @param {Integer} number Step number
*/
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
    }
};

/**
* Executed when a tag label is clicked (toggle its selection)
*
* @method onTagClicked
* @param {Dom} element Clicked element
*/
PlaceWizard.prototype.onTagClicked = function(element)
{
    if(typeof element.attr("selected") == "undefined")
    {
        this.selectTag(element);
    }
    else
    {
        this.unselectTag(element);
    }
};

/**
* Select a tag by applying a specific style to the element
*
* @method selectTag
* @param {Dom} element Tag container
*/
PlaceWizard.prototype.selectTag = function(element)
{
    element.css("background-color", element.attr("data-color"));
    element.attr("selected", "selected");
};

/**
* Unselect a tag by applying a default style to the element
*
* @method unselectTag
* @param {Dom} element Tag container
*/
PlaceWizard.prototype.unselectTag = function(element)
{
    element.removeAttr("selected");
    element.css("background-color", "");
};