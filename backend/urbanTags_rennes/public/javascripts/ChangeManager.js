/* FOR FUTURE USES */
function ChangeManager(_placeManager, _infoManager)
{
    this.placeManager = _placeManager;
    this.infoManager = _infoManager;
    
    this.placeChanges = {};
    this.infoChanges = {};
    
    this.infoChangeRegistration = this.infoManager.register("newChange", this.onNewInfoChange.bind(this));
    this.placeChangeRegistration = this.placeManager.register("newChange", this.onNewPlaceChange.bind(this));
    
    this.undoInfoChangeRegistration = this.infoManager.register("undoChange", this.onUndoInfoChange.bind(this));
    this.undoPlaceChangeRegistration = this.placeManager.register("undoChange", this.onUndoPlaceChange.bind(this));
    
    this.applyInfoChangeRegistration = this.infoManager.register("applyChange", this.onUndoInfoChange.bind(this));
    this.applyPlaceChangeRegistration = this.placeManager.register("applyChange", this.onUndoPlaceChange.bind(this));
}

ChangeManager.prototype.onNewInfoChange = function(info)
{
    this.infoChanges[info.id] = info;
    
    $("#place-list-save-changes-button").removeAttr("disabled");
    $("#place-list-save-changes-button").addClass("btn-success");
    $("#place-list-save-changes-button").attr("value", "Enregistrer les changements");
};

ChangeManager.prototype.onNewPlaceChange = function(place)
{
    this.placeChanges[place.id] = place;
    
    $("#place-list-save-changes-button").removeAttr("disabled");
    $("#place-list-save-changes-button").addClass("btn-success");
    $("#place-list-save-changes-button").attr("value", "Enregistrer les changements");
};

ChangeManager.prototype.onUndoInfoChange = function(info)
{
    delete this.infoChanges[info.id];
    
    if(Object.size(this.infoChanges) <= 0 && Object.size(this.placeChanges) <= 0)
    {
        $("#place-list-save-changes-button").attr("disabled", "disabled");
        $("#place-list-save-changes-button").removeClass("btn-success");
        $("#place-list-save-changes-button").attr("value", "Aucun changement");
    }
};
    
ChangeManager.prototype.onUndoPlaceChange = function(place)
{
    delete this.placeChanges[place.id];
    
    if(Object.size(this.infoChanges) <= 0 && Object.size(this.placeChanges) <= 0)
    {
        $("#place-list-save-changes-button").attr("disabled", "disabled");
        $("#place-list-save-changes-button").removeClass("btn-success");
        $("#place-list-save-changes-button").attr("value", "Aucun changement");
    }
};


/**
 * Ask managers to apply changes
 */
ChangeManager.prototype.applyChanges = function()
{
    // Place adding changes
       // when finished
       // Infos adding changes
    
    
    // Info deleting changes
       // when finished
       // Place deleting changes
    
    
    // Info editing changes
    
    
    // Place editing changes
};