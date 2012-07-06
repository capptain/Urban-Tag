"use strict";

function InfoList(_container, _infoManager, _infoWizard)
{
    this.container = _container;
    this.infoManager = _infoManager;
    this.infoWizard = _infoWizard;
    
    this.contents = new Object();
    
    this.infosLoadedRegistration = this.infoManager.register("infosLoaded", this.onInfosLoaded.bind(this));
    this.infoAddedRegistration = this.infoManager.register("infoAdded", this.onInfoAdded.bind(this));
    this.infoEditedRegistration = this.infoManager.register("infoEdited", this.onInfoEdited.bind(this));
    this.infoSelectedRegistration = this.infoManager.register("infoSelected", this.onInfoSelected.bind(this));
    this.infoUnselectedRegistration = this.infoManager.register("infoUnselected", this.onInfoUnselected.bind(this));
    this.infoDeletedRegistration = this.infoManager.register("infoDeleted", this.onInfoDeleted.bind(this));
    
    
    this.placeEditedRegistration = this.infoManager.manager.register("placeEdited", this.onPlaceEdited.bind(this));
    this.placeUnselectedRegistration = this.infoManager.manager.register("placeUnselected", this.onPlaceUnselected.bind(this));
    this.placeSelectedRegistration = this.infoManager.manager.register("placeSelected", this.onPlaceSelected.bind(this));
    
    this.templateView = null;
    $.get(jsRoutes.info.list.view.getTemplate(), function(data)
    {
        this.templateView = data;
    }.bind(this));
    
    $("#button-add-info", this.container).bind('click', function()
    {
        this.infoWizard.show();
    }.bind(this));
}

InfoList.prototype.reset = function()
{
    $("#info-list-content").html("");
};

InfoList.prototype.addInfo = function(info)
{
    // Get empty template
    if(this.templateView == null)
    {
        $.get(jsRoutes.info.list.view.getTemplate(), function(data)
        {
            this.templateView = data;
            this.addInfo(info);
        }.bind(this));
        return;
    }
    
    var htmlContent = "";
    
    if(typeof info.state == "undefined")
    {
        htmlContent = this.displayNormalInfo($(this.templateView), info);
    }
    else
    {
        if(info.state == "added")
        {
        
        }
        else if(info.state == "deleted")
        {
            var htmlContent = this.displayDeletedInfo($(this.templateView), info);
        }
        else if(info.state == "edited")
        {
        
        }
    }
    
    this.contents[info.id] = htmlContent;
    $("#info-list-content").append(htmlContent);
};

InfoList.prototype.editInfo = function(info)
{
    var html = this.contents[info.id];
    
    if(typeof info.state == "undefined")
    {
        htmlContent = this.displayNormalInfo(html, info);
    }
    else
    {
        if(info.state == "added")
        {
        
        }
        else if(info.state == "deleted")
        {
            var htmlContent = this.displayDeletedInfo(html, info);
        }
        else if(info.state == "edited")
        {
        
        }
    }
}

InfoList.prototype.displayEmptyPlaceMessage = function()
{
    $("#info-list-content").load(jsRoutes.info.list.view.empty());
}

InfoList.prototype.show = function()
{
    this.container.css("visibility", "visible");
};

InfoList.prototype.hide = function()
{
    this.container.css("visibility", "hidden");
};

InfoList.prototype.onInfosLoaded = function(infos)
{
    $("#info-list-content").html("");
    
    if(infos.length == 0)
    {
        this.displayEmptyPlaceMessage();
    }
    else
    {
        for(var i = 0; i < infos.length; i++)
        {
            var info = infos[i];
            this.addInfo(info);
        }
    }
};

InfoList.prototype.onInfoAdded = function(info)
{
    this.addInfo(info);
};

InfoList.prototype.onInfoEdited = function(info)
{
  this.editInfo(info);  
};

InfoList.prototype.onInfoSelected = function(info)
{
    var selectedIndex = this.infoManager.placesInfos[info.place.id].indexOf(info);
    if(selectedIndex !== -1)
    {
        jQuery('.info-summary.selected', this.container).removeClass('selected');
        jQuery('.info-summary:eq(' + selectedIndex + ')', this.container).addClass('selected');
    }
};

InfoList.prototype.onInfoUnselected = function(info)
{
    $('.info-summary.selected', this.container).removeClass("selected");
};

InfoList.prototype.onInfoDeleted = function(info)
{
    var html = this.contents[info.id];
    if(typeof html != "undefined")
    {
        html.remove();
        delete this.contents[info.id];
    }
};

InfoList.prototype.onPlaceUnselected = function(place)
{
    this.hide();
    this.reset();
};

InfoList.prototype.onPlaceSelected = function(place)
{
    this.reset();
    $("#info-list-title").html(place.name);
    $("#info-list-place-owner").html("créé par " + place.owner.username);
    
    var tagsHtml = "";
    for(var i = 0; i < place.tags.length; i++)
    {
        var tag = place.tags[i];
        tagsHtml += "<span class='label' style='background:"+tag.color+";'>"+tag.name+"</span> ";
    }
    
    $("#info-list-content").html($("<img src='/public/images/loader.gif' />"));
    
    $("#info-list-container p.tag-list").html(tagsHtml);
    this.show();
};

InfoList.prototype.onPlaceEdited = function(place)
{
    this.onPlaceSelected(place);
};

InfoList.prototype.displayTemplateInfo = function(htmlContent, info)
{
    // Put link
    $(".info-link", htmlContent).attr("href", jsRoutes.showInfo({id: info.id}));
    
    // Put title
    $(".info-title", htmlContent).html(info.title);
    
    // Put dates
    if(info.startDate != null && info.endDate != null)
    {
        $(".info-start-date", htmlContent).html(info.startDate);
        $("info-end-date", htmlContent).html(info.endDate);
    }
    else
    {
        $(".info-dates", htmlContent).addClass("static");
        $(".info-dates", htmlContent).html("Constamment valide");
    }
    
    // Put tags
    $(".tag-list", htmlContent).html("");
    for(var i = 0; i < info.tags.length; i++)
    {
        $(".tag-list", htmlContent).append($("<span class='label' style='background:"+info.tags[i].color+";'>"+info.tags[i].name+"</span>"));
        if(i < (info.tags.length -1))
        {
            $(".tag-list", htmlContent).append(", ");
        }
    }
    
    // Remove buttons if not owner
    if(info.place.owner.username != myName)
    {
        $('.btn', htmlContent).remove();
    }
    // Add buttons handlers if owner
    else
    {
        // Edit button handler
        $('.btn-edit-info', htmlContent).unbind('click');
        $('.btn-edit-info', htmlContent).click(function(){
           console.log("not yet implemented"); 
        }.bind(this));
        
        // Delete button handler
        $('.btn-delete-info', htmlContent).unbind('click');
        $('.btn-delete-info', htmlContent).click(function(){ this.onDeleteButtonClicked(info) }.bind(this));
    }
    
    $(".info-summary-content", htmlContent).html(info.content);
    return htmlContent;
};

InfoList.prototype.displayNormalInfo = function(htmlContent, info)
{
    // Get basic display
    htmlContent = this.displayTemplateInfo(htmlContent, info);
    
    // Remove undo button
    $('.btn-undo-info', htmlContent).remove();
    
    return htmlContent;
};

//
//---- For future uses (when changes will be stored in local before being saved on the server) ------//
//
//InfoList.prototype.displayAddedInfo = function(htmlContent, info)
//{
//};

//
//---- For future uses (when changes will be stored in local before being saved on the server) ------//
//
//InfoList.prototype.displayDeletedInfo = function(htmlContent, info)
//{
//    htmlContent = this.displayTemplateInfo(htmlContent, info);
//    
//    // Add 'deleted' tag
//    $('.info-title', htmlContent).append(" <span class='label tag-deleted'>Supprimée</span>");
//    
//    // Remove edit button
//    $('.btn-edit-info', htmlContent).remove();
//    
//    // Remove delete button
//    $('.btn-delete-info', htmlContent).remove();
//    
//    // Set undo button handler
//    $('.btn-undo-info', htmlContent).click( function(){ this.onRevertDeleteButtonClicked(info); }.bind(this) );
//    
//    return htmlContent;
//};

//
//---- For future uses (when changes will be stored in local before being saved on the server) ------//
//
//InfoList.prototype.displayEditedInfo = function(htmlContent, info)
//{
//};

InfoList.prototype.onDeleteButtonClicked = function(info)
{
    var htmlContent = this.contents[info.id];
    if(typeof htmlContent == "undefined")
    {
        return;
    }
    
    this.infoManager.deleteInfo(info);

//
//---- For future uses (when changes will be stored in local before being saved on the server) ------//
//  
//    var newHtml = this.displayDeletedInfo($(this.templateView), info);
//    this.contents[info.id] = newHtml;
//    htmlContent.replaceWith(newHtml);
};

//
//---- For future uses (when changes will be stored in local before being saved on the server) ------//
//
//InfoList.prototype.onRevertDeleteButtonClicked = function(info)
//{
//    var newHtml = this.displayNormalInfo($(this.templateView), info);
//    
//    var htmlContent = this.contents[info.id];
//    if(typeof htmlContent == "undefined")
//    {
//        return;
//    }
//    
//    this.infoManager.reverseDelete(info.id);
//    htmlContent.replaceWith(newHtml);
//    this.contents[info.id] = newHtml;
//};