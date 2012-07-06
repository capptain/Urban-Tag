function InfoWizard(_infoManager, _info)
{
    this.infoManager = _infoManager;
    this.data = {};
    if(typeof _info != "undefined")
    {
        this.data.id = _info.id;
        this.data.title = _info.title;
        this.data.content = _info.content;
        this.data.startDate = _info.startDate;
        this.data.endData = _info.endDate;
        this.data.tags = [];
        for(var i = 0; i < _info.tags.length; i++)
        {
            this.data.tags[i] = _info.tags[i].id;
        }
        this.data.mainTag = _info.mainTag.id;
        this.data.placeId = _info.place.id;
    }
    
    this.templateView = null;
    this.firstStepView = null;
    this.secondStepView = null;
    this.thirdStepView = null;
    
    this.templateHtml = null;
    this.firstStepHtml = null;
    this.secondStepHtml = null;
    this.thirdStepHtml = null;
}

/*
 * WIZARD
 */
InfoWizard.prototype.show = function(_info)
{
    this.data = {};
    if(typeof _info != "undefined")
    {
        this.data.id = _info.id;
        this.data.title = _info.title;
        this.data.content = _info.content;
        this.data.startDate = _info.startDate;
        this.data.endData = _info.endDate;
        this.data.tags = [];
        for(var i = 0; i < _info.tags.length; i++)
        {
            this.data.tags[i] = _info.tags[i].id;
        }
        this.data.mainTag = _info.mainTag.id;
        this.data.placeId = _info.place.id;
    }
    else
        {
            this.data.placeId = this.infoManager.manager.selectedPlace.id;
        } 
    
    if(this.templateView == null)
    {
        $.get(jsRoutes.infoWizard.getTemplate(), function(data)
        {
            this.templateView = data;
            this.templateHtml = $(this.templateView);
            this.show(_info);
        }.bind(this)).error(function(data)
        {
    
        }.bind(this));
        
        return;
    }
    
    if(this.templateHtml == null)
    {
        this.templateHtml = $(this.templateView);
    }
    
    $("body").append(this.templateHtml);
    $("#info-wizard-container").css("visibility", "visible");
    
    this.displayFirstStep(function(){ $("#info-wizard-content-container").center(); }.bind(this));
};

InfoWizard.prototype.close = function()
{
    this.templateHtml.remove();
    this.data = {};
    
    // Reset views
    this.templateHtml = null;
    this.firstStepHtml = null;
    this.secondStepHtml = null;
    this.thirdStepHtml = null;
};

/*
 * FIRST STEP
 */
InfoWizard.prototype.displayFirstStep = function(callback)
{
    // Retrieve the template if null
    if(this.firstStepView == null)
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
    
    // Set template instance if null
    if(this.firstStepHtml == null)
    {
        this.firstStepHtml = $(this.firstStepView);
    }
    
    // Display the content
    $("#info-wizard-step-content").html(this.firstStepHtml);
    
    // Set breadcrumb
    $(".custom-breadcrumb li.first", this.templateHtml).addClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).removeClass("current");
    
    // fill-in the form if data are present
    if(typeof this.data.title != "undefined")
    {
        $("#info-wizard-first-step-title", this.firstStepHtml).val(this.data.title);
    }
    if(typeof this.data.startDate != "undefined" && typeof this.data.endDate != "undefined")
    {
        $("#info-wizard-first-step-startDate", this.firstStepHtml).val(this.data.startDate);
        $("#info-wizard-first-step-endDate", this.firstStepHtml).val(this.data.endDate);
        $("#info-wizard-first-step-event", this.firstStepHtml).attr("selected", "selected");
        $("#info-wizard-first-step-static", this.firstStepHtml).removeAttr("selected");
    }
    if(typeof this.data.tags != "undefined")
    {
        for(var i = 0; i < this.data.tags.length; i++)
        {
            var tagId = this.data.tags[i];
            var elt = $("#info-wizard-first-step-tags-select option[value='"+tagId+"']");
            elt.attr("selected", "selected");
        }
    }
    if(typeof this.data.mainTag != "undefined")
    {
        var tagId = this.data.mainTag;
        var elt = $("#info-wizard-first-step-mainTag-select option[value='"+tagId+"']"); 
        elt.attr("selected", "selected");
    }
    
    // Hide error message fields
    $("#info-wizard-first-step-title-errorMsg", this.firstStepHtml).hide();
    $("#info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).hide();
    $("#info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).hide();
    $("#info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).hide();
    $("#info-wizard-first-step-tags-errorMsg", this.firstStepHtml).hide();
    $("#info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).hide();
    
    // Init handlers
    this.initFirstStepHandlers();
    
    if(typeof callback != "undefined")
    {
        callback();
    }
};

InfoWizard.prototype.initFirstStepHandlers = function()
{
    $("#info-wizard-first-step-static-radio", this.firstStepHtml).bind("click", function()
    {
        $("#info-wizard-first-step-startDate").addClass("disabled");
        $("#info-wizard-first-step-startDate").attr("disabled", "disabled");
        
        $("#info-wizard-first-step-endDate").addClass("disabled");
        $("#info-wizard-first-step-endDate").attr("disabled", "disabled");
    });
    
    $("#info-wizard-first-step-event-radio", this.firstStepHtml).bind("click", function()
    {
        $("#info-wizard-first-step-startDate").removeClass("disabled");
        $("#info-wizard-first-step-startDate").removeAttr("disabled");
        
        $("#info-wizard-first-step-endDate").removeClass("disabled");
        $("#info-wizard-first-step-endDate").removeAttr("disabled");
    });
    
    $("#info-wizard-first-step-next-btn", this.firstStepHtml).bind('click', function(){
       this.validateFirstStep();
    }.bind(this));
    
    $("#info-wizard-first-step-cancel-btn", this.firstStepHtml).bind('click', function()
    {
        this.close();
    }.bind(this));
};

InfoWizard.prototype.validateFirstStep = function()
{
    // Prepare data
    var postData = {};
    postData.title = $("#info-wizard-first-step-title", this.firstStepHtml).val();
    postData.type = $("input:radio[name='infoType']:checked", this.firstStepHtml).val();
    postData.placeId = this.data.placeId;
    if(!$("#info-wizard-first-step-startDate", this.firstStepHtml).hasClass("disabled") && !$("#info-wizard-first-step-endDate", this.firstStepHtml).hasClass("disabled"))
    {
        postData.startDate = $("#info-wizard-first-step-startDate", this.firstStepHtml).val();
        postData.endDate = $("#info-wizard-first-step-endDate", this.firstStepHtml).val();
    }
    postData.tags = $("#info-wizard-first-step-tags", this.firstStepHtml).val();
    postData.mainTag = $("#info-wizard-first-step-mainTag", this.firstStepHtml).val();
    if(typeof this.data.id != "undefined")
    {
        postData.id = this.data.id;
    }
    
    // slide up error messages
    $("#info-wizard-first-step-title-errorMsg", this.firstStepHtml).slideUp(300);
    $("#info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).slideUp(300);
    $("#info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).slideUp(300);
    $("#info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).slideUp(300);
    $("#info-wizard-first-step-tags-errorMsg", this.firstStepHtml).slideUp(300);
    $("#info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).slideUp(300);
    
    var json = JSON.stringify(postData);
    
    // Send data to the server
    $.post(jsRoutes.infoWizard.validation.firstStep({'json': json}), function(data)
    {
        // Save validated data
        this.data.title = postData.title;
        this.data.type = postData.type;
        if(typeof postData.startDate != "undefined" && typeof postData.endDate != "undefined")
        {
            this.data.startDate = postData.startDate;
            this.data.endDate = postData.endDate;
        }
        this.data.tags = postData.tags;
        this.data.mainTag = postData.mainTag;
        
        // Display next step
        this.displaySecondStep();
    }.bind(this)).error(function(data)
    {
        var json = jQuery.parseJSON(data.responseText);
        
        // Display error messages
        if(typeof json.title != "undefined")
        {
            $("#info-wizard-first-step-title-errorMsg", this.firstStepHtml).html(json.title);
            $("#info-wizard-first-step-title-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.infoType != "undefined")
        {
            $("#info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).html(json.infoType);
            $("#info-wizard-first-step-infoType-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.startDate != "undefined")
        {
            $("#info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).html(json.startDate);
            $("#info-wizard-first-step-startDate-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.endDate != "undefined")
        {
            $("#info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).html(json.endDate);
            $("#info-wizard-first-step-endDate-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.tags != "undefined")
        {
            $("#info-wizard-first-step-tags-errorMsg", this.firstStepHtml).html(json.tags);
            $("#info-wizard-first-step-tags-errorMsg", this.firstStepHtml).slideDown(300);
        }
        
        if(typeof json.mainTag != "undefined")
        {
            $("#info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).html(json.mainTag);
            $("#info-wizard-first-step-mainTag-errorMsg", this.firstStepHtml).slideDown(300);
        }
    }.bind(this));
};

/*
 * SECOND STEP
 */
InfoWizard.prototype.displaySecondStep = function()
{
    // Retrieve the template if null
    if(this.secondStepView == null)
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
    if(this.secondStepHtml == null)
    {
        this.secondStepHtml = $(this.secondStepView);
    }
    
    // Display the content
    $("#info-wizard-step-content").html(this.secondStepHtml);
    
    // fill-in the form if data are present
    if(typeof this.data.content != "undefined")
    {
        $("#info-wizard-second-step-content", this.secondStepHtml).val(this.data.content);
    }
    
    // Set breadcrumb
    $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).addClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).removeClass("current");
    
    // Hide error message fields
    $("#info-wizard-second-step-content-errorMsg", this.secondStepHtml).hide();
    
    // Init handlers
    this.initSecondStepHandlers();
};

InfoWizard.prototype.initSecondStepHandlers = function()
{
    $("#info-wizard-second-step-next-btn", this.secondStepHtml).bind('click', function(){
        this.validateSecondStep();
    }.bind(this));
    $("#info-wizard-second-step-previous-btn", this.secondStepHtml).bind('click', function(){
        this.displayFirstStep();
    }.bind(this));
    $("#info-wizard-second-step-cancel-btn", this.secondStepHtml).bind('click', function(){
        this.close();
    }.bind(this));
};

InfoWizard.prototype.validateSecondStep = function()
{
    // Prepare data
    var postData = {};
    postData.content = $("#info-wizard-second-step-content", this.secondStepHtml).val();
    
    // Hide error message fields
    $("#info-wizard-second-step-content-errorMsg", this.secondStepHtml).slideUp(300);
    
    // Send data to the server
    $.post(jsRoutes.infoWizard.validation.secondStep({'json': JSON.stringify(postData)}), function(data){
        this.data.content = postData.content;
        this.displayThirdStep();
    }.bind(this)).error(function(data){
        var json = jQuery.parseJSON(data.responseText);
        
        // Display error messages
        if(typeof json.content != "undefined")
        {
            $("#info-wizard-second-step-content-errorMsg", this.secondStepHtml).html(json.content);
            $("#info-wizard-second-step-content-errorMsg", this.secondStepHtml).slideDown(300);
        }
    }.bind(this));
};

/*
 * THIRD STEP
 */
InfoWizard.prototype.displayThirdStep = function()
{
    // Retrieve the template if null
    if(this.thirdStepView == null)
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
    if(this.thirdStepHtml == null)
    {
        this.thirdStepHtml = $(this.thirdStepView);
    }
    
    // Display the content
    $("#info-wizard-step-content").html(this.thirdStepHtml);
    
    // Set breadcrumb
    $(".custom-breadcrumb li.first", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.second", this.templateHtml).removeClass("current");
    $(".custom-breadcrumb li.third", this.templateHtml).addClass("current");
    
    // Hide error message fields
    $("#info-wizard-third-step-errorMsg", this.thirdStepHtml).hide();
    
    // Init handlers
    this.initThirdStepHandlers();
};

InfoWizard.prototype.initThirdStepHandlers = function()
{
    $("#info-wizard-third-step-next-btn", this.thirdStepHtml).bind('click', function(){
        this.save();
    }.bind(this));
    $("#info-wizard-third-step-previous-btn", this.thirdStepHtml).bind('click', function(){
        this.displaySecondStep();
    }.bind(this));
    $("#info-wizard-third-step-cancel-btn", this.thirdStepHtml).bind('click', function(){
        this.close();
    }.bind(this));
};

InfoWizard.prototype.save = function()
{
    var json = JSON.stringify(this.data);
    // Send data to save the info
    $.post(jsRoutes.info.add({'json': json}), function(data){
        
        // TODO: refresh view
        this.close();
    }.bind(this)).error(function(data)
    {
        $("#info-wizard-third-step-errorMsg", this.thirdStepHtml).html(data.message);
    }.bind(this));
};