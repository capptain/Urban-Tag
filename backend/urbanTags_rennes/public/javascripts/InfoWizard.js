(function(){
    "use strict";
})();

function InfoWizard(info)
{
    CanFireEvents.call(this, ["infoAdded", "infoEdited"]);
    
    this.data = {};
    this.mode = "description";
    
    if(typeof info != "undefined"){
        this.data.id      = info.id;
        this.data.title   = info.title;
        this.data.content = info.content;
        
        if(typeof info.startDate != "undefined" && typeof info.endDate != "undefined")
        {
            this.data.startDate = info.startDate;
            this.data.endData   = info.endDate;
            this.mode           = "event";
        }
        this.data.tags = [];
        
        for(var i = 0; i < info.tags.length; i++){
            this.data.tags[i] = info.tags[i].id;
        }
        this.data.mainTag = info.mainTag.id;
        this.data.placeId = info.place.id;
    }
    this.templateView   = null;
    this.firstStepView  = null;
    this.secondStepView = null;
    this.thirdStepView  = null;
    
    this.templateHtml   = null;
    this.firstStepHtml  = null;
    this.secondStepHtml = null;
    this.thirdStepHtml  = null;
    
    /* Used to know which tag was previously selected as a main tag in the first step when selection changes */
    this.previousMainTag = null;
}

extend(InfoWizard.prototype, CanFireEvents.prototype);

/*
 * WIZARD
 */
InfoWizard.prototype.startCreatingEvent = function(place)
{
    this.data         = {};
    this.data.placeId = place.id;
    this.mode         = "event";
    this.show();
};

InfoWizard.prototype.startEditingEvent = function(event)
{
    this.data.id      = event.id;
    this.data.title   = event.title;
    this.data.content = event.content;
    this.mode         = "event";
    
    var startDate       = new Date(getDateFromFormat(event.startDate, "dd/MM/yyyy HH:mm"));
    var endDate         = new Date(getDateFromFormat(event.endDate, "dd/MM/yyyy HH:mm"));
    this.data.startDate = formatDate(startDate, "dd/MM/yyyy");
    this.data.endDate   = formatDate(endDate, "dd/MM/yyyy");
    this.data.startTime = formatDate(startDate, "HH:mm");
    this.data.endTime   = formatDate(endDate, "HH:mm");
    
    this.data.tags = [];
    for(var i = 0; i < event.tags.length; i++){
        this.data.tags[i] = event.tags[i].id;
    }
    this.data.mainTag = event.mainTag.id;
    this.data.placeId = event.place.id;
    
    this.show();
};

InfoWizard.prototype.startCreatingDescription = function(place)
{
    this.data         = {};
    this.data.title   = place.name;
    this.data.placeId = place.id;
    this.mode         = "description";
    this.data.tags    = [];
    for(var i = 0; i < place.tags.length; i++){
        this.data.tags[i] = place.tags[i].id;
    }
    this.data.mainTag = place.mainTag.id;
    
    this.show();
};

InfoWizard.prototype.startEditingDescription = function(description)
{
    this.data.id      = description.id;
    this.data.title   = description.title;
    this.data.content = description.content;
    this.mode         = "description";
    this.data.tags    = [];
    for(var i = 0; i < description.tags.length; i++)
    {
        this.data.tags[i] = description.tags[i].id;
    }
    this.data.mainTag = description.mainTag.id;
    this.data.placeId = description.place.id;
    
    this.show();
};

InfoWizard.prototype.show = function()
{
    if(this.templateView === null)
    {
        $.get(jsRoutes.infoWizard.getTemplate(), function(data)
        {
            this.templateView = data;
            this.show();
        }.bind(this)).error(function(data)
        {
            // TODO: handle error
        }.bind(this));
        
        return;
    }
    
    if(this.templateHtml === null)
    {
        this.templateHtml = $(this.templateView);
        $("body").append(this.templateHtml);
    }
    
    if(this.mode === "description")
    {
        $(".info-wizard-title", this.templateHtml).html("Décrire le lieu");
        $(".info-wizard-evt-breadcrumb", this.templateHtml).hide();
        $(".info-wizard-description-breadcrumb", this.templateHtml).show();
        
        this.displaySecondStep();
    }
    else if(this.mode === "event")
    {
        if(typeof this.data.id == "undefined")
            $(".info-wizard-title", this.templateHtml).html("Ajouter un événement");
        else
            $(".info-wizard-title", this.templateHtml).html("Modifier un événement");
        
        $(".info-wizard-evt-breadcrumb", this.templateHtml).show();
        $(".info-wizard-description-breadcrumb", this.templateHtml).hide();
        this.displayFirstStep();
    }
    
    this.templateHtml.modal({'show': true, 'backdrop': 'static'});
};

InfoWizard.prototype.close = function()
{
    this.templateHtml.modal('hide');
    this.data = {};
    
    // Reset views
    this.firstStepHtml  = null;
    this.secondStepHtml = null;
    this.thirdStepHtml  = null;
};

/*
 * FIRST STEP
 */
InfoWizard.prototype.displayFirstStep = function()
{
    // Retrieve the template if null
    if(this.firstStepView === null)
    {
        $.get(jsRoutes.infoWizard.firstStep(), function(data)
        {
            this.firstStepView = data;
            this.displayFirstStep();
        }.bind(this)).error(function(data)
        {
            // TODO: handler error
        }.bind(this));
        
        return;
    }
    
    /* Set template instance if null */
    if(this.firstStepHtml === null)
        this.firstStepHtml = $(this.firstStepView);
    
    /* Display the content */
    $(".info-wizard-step-content", this.templateHtml).html(this.firstStepHtml);
    
    /* Set breadcrumb */
    $(".custom-breadcrumb li.first", this.templateHtml).addClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).removeClass("current");
    
    /* fill-in the form if data are present */
    if(typeof this.data.title != "undefined")
    {
        $(".info-wizard-first-step-title", this.firstStepHtml).val(this.data.title);
    }
    
    var defaultTime = "current";
    if(typeof this.data.startDate != "undefined" && typeof this.data.endDate != "undefined" && typeof this.data.startTime != "undefined" && typeof this.data.endTime != "undefined")
    {
        $(".info-wizard-first-step-startDate", this.firstStepHtml).val(this.data.startDate);
        $(".info-wizard-first-step-endDate", this.firstStepHtml).val(this.data.endDate);
        $(".info-wizard-first-step-startTime", this.firstStepHtml).val(this.data.startTime);
        $(".info-wizard-first-step-endTime", this.firstStepHtml).val(this.data.endTime);
        defaultTime = "value";
    }
    
    if(typeof this.data.tags != "undefined")
    {
        for(var i = 0; i < this.data.tags.length; i++)
        {
            var tagId = this.data.tags[i];
            var tagElt   = $(".info-wizard-first-step-tags-container a[data-value='"+tagId+"']", this.firstStepHtml);
            this.selectTag(tagElt);
        }
    }
    
    if(typeof this.data.mainTag != "undefined")
    {
        var obj = this;
        $(".info-wizard-first-step-mainTag-container a", this.firstStepHtml).each(function(index, object)
        {
            obj.unselectTag($(object));
        });
        
        var mainTagId = this.data.mainTag;
        var mainTagElt = $(".info-wizard-first-step-mainTag-container a[data-value='"+mainTagId+"']", this.firstStepHtml);
        this.selectTag(mainTagElt);
        
        var newTagElement = $(".info-wizard-first-step-tags-container a[data-value='"+mainTagId+"']", this.firstStepHtml);
        newTagElement.css("opacity", "0.4");
        this.selectTag(newTagElement);
        this.previousMainTag = mainTagId;
    }
    
    // Hide error message fields
    $(".info-wizard-first-step-title-errorMsg", this.firstStepHtml).hide();
    $(".info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).hide();
    $(".info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).hide();
    $(".info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).hide();
    $(".info-wizard-first-step-tags-errorMsg", this.firstStepHtml).hide();
    $(".info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).hide();
    
    $('.info-wizard-first-step-startDate', this.firstStepHtml).datepicker({format: 'dd/mm/yyyy', weekStart: 1});
    $('.info-wizard-first-step-endDate', this.firstStepHtml).datepicker({format: 'dd/mm/yyyy', weekStart: 1});
    $('.info-wizard-first-step-startTime', this.firstStepHtml).timepicker({'template': 'dropdown', 'minuteStep': 15, 'showMeridian': false, 'defaultTime': defaultTime});
    $('.info-wizard-first-step-endTime', this.firstStepHtml).timepicker({'template': 'dropdown', 'minuteStep': 15, 'showMeridian': false, 'defaultTime': defaultTime});
    
    // Init handlers
    this.initFirstStepHandlers();
};

InfoWizard.prototype.initFirstStepHandlers = function()
{
    /* Manage buttons */
    $(".info-wizard-previous-btn", this.templateHtml).hide();
    $(".info-wizard-cancel-btn", this.templateHtml).unbind("click");
    $(".info-wizard-previous-btn", this.templateHtml).unbind("click");
    $(".info-wizard-next-btn", this.templateHtml).unbind("click");
    $(".info-wizard-next-btn", this.templateHtml).bind('click', function()
    {
       this.validateFirstStep();
    }.bind(this));
    $(".info-wizard-cancel-btn", this.templateHtml).bind('click', function()
    {
        this.close();
    }.bind(this));
    
    var obj = this;
    
    /* Main tag handlers */
    $(".info-wizard-first-step-mainTag-container a", this.firstStepHtml).bind('click', function()
    {
        $(".info-wizard-first-step-mainTag-container a", this.firstStepHtml).each(function(index, object)
        {
           obj.unselectTag($(object));
        });
        obj.onTagClicked($(this));
        if(obj.previousMainTag !== null)
        {
            var previousTagElement = $(".info-wizard-first-step-tags-container a[data-value='"+obj.previousMainTag + "']", this.firstStepHtml);
            previousTagElement.css("opacity", "1");
            obj.unselectTag(previousTagElement);
            
            previousTagElement.bind('click', function()
            {
                obj.onTagClicked($(this));
            });
        }
        
        var mainTagId       = $(this).attr("data-value");
        var newTagElement   = $(".info-wizard-first-step-tags-container a[data-value='"+mainTagId+"']", this.firstStepHtml);
        newTagElement.css("opacity", "0.4");
        newTagElement.unbind('click');
        obj.selectTag(newTagElement);
        obj.previousMainTag = mainTagId;
    });
    
    /* Tag handlers */
    $(".info-wizard-first-step-tags-container a", this.firstStepHtml).bind('click', function()
    {
        obj.onTagClicked($(this));
    });
    
    $(".info-wizard-first-step-tags", this.firstStepHtml).bind('change', function()
    {
        var mainTagId = $(".info-wizard-first-step-mainTag option:selected", this.firstStepHtml).val();
        if(typeof $(".info-wizard-first-step-tags option[value='"+ mainTagId +"']", this.firstStepHtml).attr('selected') == "undefined"){
            $(".info-wizard-first-step-tags option[value='"+ mainTagId +"']", this.firstStepHtml).attr('selected', 'selected');
        }
    }.bind(this));
    $(".info-wizard-first-step-tags-container a[data-value='"+this.previousMainTag+"']", this.firstStepHtml).unbind('click');
};

InfoWizard.prototype.validateFirstStep = function()
{
    // Prepare data
    var postData       = {};
    postData.title     = $(".info-wizard-first-step-title", this.firstStepHtml).val();
    postData.placeId   = this.data.placeId;
    postData.startDate = $(".info-wizard-first-step-startDate", this.firstStepHtml).val();
    postData.endDate   = $(".info-wizard-first-step-endDate", this.firstStepHtml).val();
    postData.startTime = $(".info-wizard-first-step-startTime", this.firstStepHtml).val();
    postData.endTime   = $(".info-wizard-first-step-endTime", this.firstStepHtml).val();
    
    postData.tags = [];
    $(".info-wizard-first-step-tags-container a", this.firstStepHtml).each(function(index, element){
        if(typeof $(element).attr("selected") != "undefined"){
            postData.tags.push($(element).attr("data-value"));
        }
    });
    postData.mainTag = $(".info-wizard-first-step-mainTag-container a[selected='selected']", this.firstStepHtml).attr("data-value");
    if(typeof this.data.id != "undefined"){
        postData.id = this.data.id;
    }
    
    // slide up error messages
    $(".info-wizard-first-step-title-errorMsg", this.firstStepHtml).slideUp(300);
    $(".info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).slideUp(300);
    $(".info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).slideUp(300);
    $(".info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).slideUp(300);
    $(".info-wizard-first-step-tags-errorMsg", this.firstStepHtml).slideUp(300);
    $(".info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).slideUp(300);
    
    var json = JSON.stringify(postData);
    
    // Send data to the server
    $.post(jsRoutes.infoWizard.validation.firstStep({'json': json}), function(data)
    {
        // Save validated data
        this.data.title     = postData.title;
        this.data.startDate = postData.startDate;
        this.data.endDate   = postData.endDate;
        this.data.startTime = postData.startTime;
        this.data.endTime   = postData.endTime;
        this.data.tags      = postData.tags;
        this.data.mainTag   = postData.mainTag;
        
        // Display next step
        this.displaySecondStep();
    }.bind(this)).error(function(data)
    {
        var json = jQuery.parseJSON(data.responseText);
        
        // Display error messages
        if(typeof json.title != "undefined")
        {
            $(".info-wizard-first-step-title-errorMsg", this.firstStepHtml).html(json.title);
            $(".info-wizard-first-step-title-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.infoType != "undefined")
        {
            $(".info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).html(json.infoType);
            $(".info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.startDate != "undefined")
        {
            $(".info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).html(json.startDate);
            $(".info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.endDate != "undefined")
        {
            $(".info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).html(json.endDate);
            $(".info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.tags != "undefined")
        {
            $(".info-wizard-first-step-tags-errorMsg", this.firstStepHtml).html(json.tags);
            $(".info-wizard-first-step-tags-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.mainTag != "undefined")
        {
            $(".info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).html(json.mainTag);
            $(".info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).slideDown(300);
        }
    }.bind(this));
};

/*
 * SECOND STEP
 */
InfoWizard.prototype.displaySecondStep = function()
{
    // Retrieve the template if null
    if(this.secondStepView === null)
    {
        $.get(jsRoutes.infoWizard.secondStep(), function(data)
        {
            this.secondStepView = data;
            this.displaySecondStep();
        }.bind(this)).error(function(data)
        {
            //TODO: handle error
        }.bind(this));
        
        return;
    }
    
    // Set template instance if null
    if(this.secondStepHtml === null)
    {
        this.secondStepHtml = $(this.secondStepView);
        $(".info-wizard-second-step-content", this.secondStepHtml).markItUp(mySettings);
    }
    
    // Display the content
    $(".info-wizard-step-content", this.templateHtml).html(this.secondStepHtml);
    
    // Fill-in the form if data are present
    if(typeof this.data.content != "undefined")
    {
        $(".info-wizard-second-step-content", this.secondStepHtml).val(this.data.content);
    }
    
    if(this.mode == "event")
    {
        $(".info-wizard-content-step-subtitle", this.templateHtml).html("Evénement");
        // Set breadcrumb
        $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
        $(".custom-breadcrumb li.second", this.templateHtml).addClass("current");
        $(".custom-breadcrumb li.third", this.templateHtml).removeClass("current");
    }
    else if(this.mode == "description")
    {
        $(".info-wizard-content-step-subtitle", this.templateHtml).html("Description");
         // Set breadcrumb
        $(".custom-breadcrumb li.first", this.templateHtml).addClass("current");
        $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    }
    
    
    // Hide error message fields
    $(".info-wizard-second-step-content-errorMsg", this.secondStepHtml).hide();
    
    // Init handlers
    this.initSecondStepHandlers();
};

InfoWizard.prototype.initSecondStepHandlers = function()
{
    $(".info-wizard-cancel-btn", this.templateHtml).unbind("click");
    $(".info-wizard-previous-btn", this.templateHtml).unbind("click");
    $(".info-wizard-next-btn", this.templateHtml).unbind("click");
    
    if(this.mode === "event")
    {
        $(".info-wizard-previous-btn", this.templateHtml).show();
        $(".info-wizard-previous-btn", this.templateHtml).bind('click', function(){
            this.displayFirstStep();
        }.bind(this));
    }
    else
    {
        $(".info-wizard-previous-btn", this.templateHtml).hide();
    }
    
    $(".info-wizard-next-btn", this.templateHtml).bind('click', function(){
        this.validateSecondStep();
    }.bind(this));
    $(".info-wizard-cancel-btn", this.templateHtml).bind('click', function(){
        this.close();
    }.bind(this));
};

InfoWizard.prototype.validateSecondStep = function()
{
    // Prepare data
    var postData = {};
    postData.content = $(".info-wizard-second-step-content", this.secondStepHtml).val();
    
    // Hide error message fields
    $(".info-wizard-second-step-content-errorMsg", this.secondStepHtml).slideUp(300);
    
    // Send data to the server
    $.post(jsRoutes.infoWizard.validation.secondStep({'json': JSON.stringify(postData)}), function(data){
        this.data.content = postData.content;
        this.displayThirdStep();
    }.bind(this)).error(function(data){
        var json = jQuery.parseJSON(data.responseText);
        
        // Display error messages
        if(typeof json.content != "undefined")
        {
            $(".info-wizard-second-step-content-errorMsg", this.secondStepHtml).html(json.content);
            $(".info-wizard-second-step-content-errorMsg", this.secondStepHtml).slideDown(300);
        }
    }.bind(this));
};

/*
 * THIRD STEP
 */
InfoWizard.prototype.displayThirdStep = function()
{
    // Retrieve the template if null
    if(this.thirdStepView === null)
    {
        $.get(jsRoutes.infoWizard.thirdStep(), function(data)
        {
            this.thirdStepView = data;
            this.displayThirdStep();
        }.bind(this)).error(function(data)
        {
            // TODO: display error
        }.bind(this));
        
        return;
    }
    // Set template instance if null
    if(this.thirdStepHtml === null)
    {
        this.thirdStepHtml = $(this.thirdStepView);
    }
    
    // Display the content
    $(".info-wizard-step-content", this.templateHtml).html(this.thirdStepHtml);
    
    // Hide alert fields
    $(".alert", this.thirdStepHtml).hide();
    
    // Fill-in the form with Info's data
    $(".info-wizard-third-step-title", this.thirdStepHtml).html(this.data.title);
    
    if(typeof this.data.startDate != "undefined" && typeof this.data.endDate != "undefined")
    {
        $(".info-wizard-third-step-duration", this.thirdStepHtml).html("Du " + this.data.startDate + " " + this.data.startTime + " au " + this.data.endDate + " " + this.data.endTime);
    }
    else
    {
        $(".info-wizard-third-step-duration", this.thirdStepHtml).html("Toujours valide");
    }
    
    var postData = this.data.tags;
    postData.push(this.data.mainTag);
    $.post(jsRoutes.tag.getArray({'json': JSON.stringify(postData)}),
            function(data)
            {
                var content = "";
                for(var i=0; i < data.length; i++)
                {
                    var tag = data[i];
                    if(tag.id == this.data.mainTag){
                        $(".info-wizard-third-step-mainTag-container", this.thirdStepHtml).html("<span class='label' style='background:#"+tag.color+";'>"+tag.name+"</span>");
                    }
                    else{
                        content += "<span class='label' style='background:#"+tag.color+";'>"+tag.name+"</span> ";
                    }
                }
                $(".info-wizard-third-step-tags-container", this.thirdStepHtml).html(content);
            }.bind(this)).error(
            function(data)
            {
                console.log("error : " + data);
                // TODO: handles error
            }
    );
    
    $(".info-wizard-third-step-content", this.thirdStepHtml).html(this.data.content);
    
    if(this.mode === "event")
    {
        // Set breadcrumb
        $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
        $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
        $(".custom-breadcrumb li.third", this.templateHtml).addClass("current");
    }
    else if(this.mode === "description")
    {
         // Set breadcrumb
        $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
        $(".custom-breadcrumb li.second", this.templateHtml).addClass("current");
    }
    
    // Hide error message fields
    $(".info-wizard-third-step-errorMsg", this.thirdStepHtml).hide();
    
    // Init handlers
    this.initThirdStepHandlers();
};

InfoWizard.prototype.initThirdStepHandlers = function()
{
    $(".info-wizard-cancel-btn", this.templateHtml).unbind("click");
    $(".info-wizard-previous-btn", this.templateHtml).unbind("click");
    $(".info-wizard-next-btn", this.templateHtml).unbind("click");
    
    $(".info-wizard-next-btn", this.templateHtml).bind('click', function(){
        this.save();
    }.bind(this));
    $(".info-wizard-previous-btn", this.templateHtml).bind('click', function(){
        this.displaySecondStep();
    }.bind(this));
    $(".info-wizard-cancel-btn", this.templateHtml).bind('click', function(){
        this.close();
    }.bind(this));
    
    if(this.mode == "description")
    {
        $(".info-wizard-previous-btn", this.templateHtml).show();
        $(".info-wizard-previous-btn", this.templateHtml).bind('click', function(){
            this.displaySecondStep();
        }.bind(this));
    }
};

InfoWizard.prototype.save = function()
{
    var json = JSON.stringify(this.data);
    $.post(jsRoutes.info.add({'json': json}), function(data){
        if(typeof this.data.id == "undefined")
        {
            this.fireEvent("infoAdded", data);
        }
        else
        {
            this.fireEvent("infoEdited", data);
        }
        this.close();
    }.bind(this)).error(function(data)
    {
        $(".info-wizard-third-step-alert-container", this.thirdStepHtml).html("<div class='alert alert-error'><button type='button' class='close' data-dismiss='alert'>x</button>Les données semblent incorrectes, nous vous conseillons de revenir à l'étape 1 pour les valider.</div>");
    }.bind(this));
};

InfoWizard.prototype.onTagClicked = function(element)
{
    if(typeof element.attr("selected") == "undefined"){
        this.selectTag(element);
    }
    else{
        this.unselectTag(element);
    }
};

InfoWizard.prototype.selectTag = function(element)
{
    element.css("background-color", element.attr("data-color"));
    element.attr("selected", "selected");
};

InfoWizard.prototype.unselectTag = function(element)
{
    element.removeAttr("selected");
    element.css("background-color", "");
};