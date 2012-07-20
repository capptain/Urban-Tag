function PlaceSummary(data)
{
    this.container = $("body");
    this.place     = null;

    if(typeof data != "undefined")
    {
        if(typeof data.placeManager != "undefined"){
            this.placeManager = data.placeManager;
            // this.placeSelectedRegistration   = this.placeManager.register("placeSelected", this.onPlaceSelected.bind(this));
            // this.placeUnselectedRegistration = this.placeManager.register("placeUnselected", this.onPlaceUnselected.bind(this));
        }

        if(typeof data.container != "undefined"){
            this.container = data.container;
        }

        if(typeof data.place != "undefined"){
            this.place = data.place;
        }
    }
    
    this.infoWizard             = new InfoWizard();
    this.infoAddedRegistration  = this.infoWizard.register("infoAdded", this.onInfoAdded.bind(this));
    this.infoEditedRegistration = this.infoWizard.register("infoEdited", this.onInfoEdited.bind(this));
    
    this.templateView = null;
    this.templateHtml = null;
    
    this.description = null;
}

PlaceSummary.prototype.show = function()
{
   if(this.templateView === null)
   {
       $.get(jsRoutes.place.summary.getTemplate(), function(data)
       {
           this.templateView = data;
           this.show();
       }.bind(this)).error(function(data)
       {
           // TODO: handle error
       }.bind(this));
       return;
   }
   
   this.templateHtml = $(this.templateView);
   this.container.html(this.templateHtml);
   
   $(".place-summary-button-add-description-info", this.templateHtml).bind('click', this.onAddButtonClicked.bind(this));
   $(".place-summary-button-edit-description-info", this.templateHtml).bind('click', this.onEditButtonClicked.bind(this));
   $(".place-summary-button-delete-description-info", this.templateHtml).bind('click', this.onDeleteButtonClicked.bind(this));
   
   this.loadPlaceSummary();
};

PlaceSummary.prototype.hide = function()
{
    if(this.templateHtml !== null)
        this.templateHtml.hide();
};

PlaceSummary.prototype.displayEmptyDescriptionMessage = function()
{
    if(this.emptyMessage === null)
    {
        $(".place-summary-loader", this.templateHtml).show();
        $.get(jsRoutes.place.summary.emptyView(), function(data)
        {
            $(".place-summary-loader", this.templateHtml).hide();
            this.emptyMessage = $(data);
            this.displayEmptyDescriptionMessage();
        }.bind(this));
        return;
    }
    
    $(".place-summary-description", this.templateHtml).html(this.emptyMessage);
};

PlaceSummary.prototype.loadPlaceSummary = function()
{
    if(this.place !== null)
    {
        /*
         * Fill in template with place informations
         */
        var titleContainer = $(".place-summary-title", this.templateHtml);
        $(".place-summary-title", this.templateHtml).html(this.place.name);
        $(".place-summary-owner", this.templateHtml).html("créé par " + this.place.owner.username);
        var tagsHtml = "";
        for(var i = 0; i < this.place.tags.length; i++)
        {
            var tag = this.place.tags[i];
            tagsHtml += "<span class='label' style='background:#"+tag.color+";'>"+tag.name+"</span> ";
        }
        $("p.tag-list", this.templateHtml).html(tagsHtml);
        
        /*
         * Load place description
         */
        $(".place-summary-button-edit-description-info", this.templateHtml).hide();
        $(".place-summary-button-add-description-info", this.templateHtml).hide();
        $(".place-summary-button-delete-description-info", this.templateHtml).hide();
        $.get(jsRoutes.getPlaceDescription({'placeId': this.place.id}), function(data)
        {
            $(".place-summary-loader", this.templateHtml).hide();
            if(data !== "" && typeof data.content != "undefined")
            {
                this.description = data;
                if(typeof myName != "undefined"){
                    if(this.place.owner.username == myName)
                    {
                        $(".place-summary-button-edit-description-info", this.templateHtml).show();
                        $(".place-summary-button-delete-description-info", this.templateHtml).show();
                    }
                }
                $(".place-summary-description", this.templateHtml).html(data.content);
            }
            else
            {
                if(this.place.owner.username == myName)
                {
                    $(".place-summary-button-add-description-info", this.templateHtml).show();
                }
                this.displayEmptyDescriptionMessage();
            }
        }.bind(this)).error(function(data){
            // TODO: manager error
        }.bind(this));
    }
    else
    {
        this.hide();
    }
};

PlaceSummary.prototype.onAddButtonClicked = function()
{
    this.infoWizard.startCreatingDescription(this.place);
};

PlaceSummary.prototype.onEditButtonClicked = function()
{
    this.infoWizard.startEditingDescription(this.description);
};

PlaceSummary.prototype.onDeleteButtonClicked = function()
{
    $.get(jsRoutes.info.remove({'id': this.description.id}), function(data)
    {
        this.onPlaceSelected(this.place);
    }.bind(this)).error(function(data){
        
    }.bind(this));
};

PlaceSummary.prototype.onPlaceSelected = function(place)
{
    this.hide();
    this.place = place;
    this.show();
};

PlaceSummary.prototype.onPlaceUnselected = function(place)
{
    this.hide();
    this.place = null;
};

PlaceSummary.prototype.onInfoAdded = function(info)
{
    this.show();
};

PlaceSummary.prototype.onInfoEdited = function(info)
{
    this.show();
};