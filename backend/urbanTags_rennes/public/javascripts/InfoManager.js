"use strict";

function InfoManager(_placeManager){
    CanFireEvents.call(this, ["infoAdded", "infoEdited", "infoDeleted", "infoSelected", "infoUnselected", "infoListShown", "infoListHidden", "infosLoaded", "newChange", "undoChange"]);
    
    this.manager = _placeManager;
    this.placesInfos = new Object();
    this.infosMap = new Object();
    this.selectedInfo = null;
    this.onPlaceSelectedRegistration = this.manager.register("placeSelected", this.onPlaceSelected.bind(this));
    this.onPlaceEditedRegistration = this.manager.register("placeEdited", this.onPlaceEdited.bind(this));
    this.addedInfos = {};
}

extend(InfoManager.prototype, CanFireEvents.prototype);

InfoManager.prototype.addInfo = function(info, fireEvent)
{
    // Create the store for the place's actions if needed
    if(typeof this.placesInfos[info.place.id] == "undefined")
    {
        this.placesInfos[info.place.id] = new Array();
    }
    
    // Store the info into the associated data structure only if not modified/deleted
    if(typeof this.infosMap[info.id] == "undefined")
    {
        this.infosMap[info.id] = info;
        
        // Push the id of the info into the ordered data structure
        this.placesInfos[info.place.id].push(info.id);
        
        // Fire an event if asked
        if(typeof fireEvent != "undefined" && fireEvent === true)
        {
            this.fireEvent("infoAdded", info);
        }
    }
};

InfoManager.prototype.reset = function()
{
    this.placesInfos = new Object();
    this.infosMap = new Object();
};

InfoManager.prototype.selectInfoByIndex = function(placeId, index)
{
    var infos = this.placesInfos[placeId];
    
    if(index >= 0 && index < infos.length)
    {
        this.selectedInfo = this.placesInfos[placeId][index];
        this.fireEvent("infoSelected", this.selectedInfo);
    }
};

InfoManager.prototype.unselect = function()
{
    var oldSelection = this.selectedInfo;
    this.selectedInfo = null;
    this.fireEvent("infoUnselected", oldSelection);
};

/**
 * Loads infos 
 */
InfoManager.prototype.loadPlaceInfos = function(_place, _from, _to)
{
    this.placesInfos[_place.id] = new Array();
    
    var from = _from;
    if(typeof _from == "undefined")
    {
        from = 0;
    }
    
    var to = _to;
    if(typeof _to == "undefined")
    {
        to = -1;
    }
    
    var obj = this;
    $.get(jsRoutes.getPlaceInfosAction({idPlace: _place.id, from: from, to: to}), function(data){
        jQuery.each(data, function(i, info)
        {
            obj.addInfo(this, false);
        });
        
        var infoList = new Array();
        for(var i = 0; i < obj.placesInfos[_place.id].length; i++)
        {
            var id = obj.placesInfos[_place.id][i];
            infoList.push(obj.infosMap[id]);
        }
        
        // Fire an event indicating that infos are loaded
        obj.fireEvent("infosLoaded", infoList);
    });
};

InfoManager.prototype.deleteInfo = function(info)
{
    var storedInfo = this.infosMap[info.id];    
    $.get(jsRoutes.info.remove({'id':info.id}), function(data){
        this.fireEvent("infoDeleted", info);
    }.bind(this)).error(function(data){
        console.log(data);
        // TODO: handles error
    }.bind(this));
    
};

InfoManager.prototype.editInfo = function(info)
{
    this.infosMap[info.id] = info;
    this.fireEvent("infoEdited", info);
};