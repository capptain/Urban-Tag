    // "use strict";

/**
* This class represents the display of the place's events list
*
* @class InfoList
* @contructor
* @param {DOM} container Container of the event list view
* @param {Object} place Focused place
*/
function InfoList(container, place)
{
    CanFireEvents.call(this, ["infoSelected", "infoUnselected"]);
    this.container = container;
    this.place = null;
    this.infoWizard = new InfoWizard();
    
    this.contents = {};
    this.events = {};
    
    this.templateView = null;
    this.templateHtml = null;
    
    this.itemView = null;
    this.selectedEvent = null;

    this.initGUI(function(){
        if(typeof place != "undefined" && place !== null)
        {
            this.setPlace(place);
        }
    }.bind(this));

    this.infoAddedRegistration = this.infoWizard.register("infoAdded", this.addEvent.bind(this));
    this.infoEditedRegistration = this.infoWizard.register("infoEdited", this.updateEvent.bind(this));
}

/* This class can fire event, we make the extension of its prototype */
extend(InfoList.prototype, CanFireEvents.prototype);

/**
* This method initialize the GUI by getting the template view from the server and initializing GUI components
*
* @method initGUI
* @param {Function} callback Optional callback
*/
InfoList.prototype.initGUI = function(callback)
{
    if(this.templateView === null){
        $.get(jsRoutes.info.list.view.getTemplate(), function(data)
        {
            this.templateView = data;
            this.initGUI();
        }.bind(this)).error(function(data){
            if(typeof data.status != "undefined" && data.status === 0)
            {
                displayAlert("Connexion avec le serveur rompue, impossible de récupérer la vue de la liste d'événements.");
            }
            else
            {
                displayAlert("Oups, erreur inconnue");
            }
        });
        
        return;
    }
    
    if(this.templateHtml === null)
    {
        this.templateHtml= $(this.templateView);
        this.container.html(this.templateHtml);
    }

    $(".info-list-filter-content", this.container).hide();

    if(typeof callback != "undefined")
    {
        callback();
    }
};

/**
* Init handlers of the GUI (action buttons if connected, filters)
*
* @method initGUIHandlers
*/
InfoList.prototype.initGUIHandlers = function()
{
    $("#info-list-add-button", this.container).bind("click", function()
    {
        this.infoWizard.startCreatingEvent(this.place);
    }.bind(this));

    $("#info-list-edit-button", this.container).bind("click", function()
    {
        this.infoWizard.startEditingEvent(this.selectedEvent);
    }.bind(this));

    $("#info-list-delete-button", this.container).bind("click", function()
    {
        showLoader($(".button-container", this.templateHtml));
        $.get(jsRoutes.info.remove({'id': this.selectedEvent.id}), function(data){
            hideLoader($(".button-container", this.templateHtml));
            this.onInfoDeleted(this.selectedEvent);
        }.bind(this)).error(function(data)
        {
            hideLoader($(".button-container", this.templateHtml));
            if(typeof data.status != "undefined" && data.status === 0)
            {
                displayAlert("La connexion avec le serveur a été rompue, merci de réessayer dans quelques instants.");
            }
            else
            {
                displayAlert("Oups, erreur inconnue.");
            }
        });
    }.bind(this));

    $(".info-list-filter-startDate", this.container).val(formatDate(new Date(), "dd/MM/yyyy"));
    $(".info-list-filter-endDate", this.container).val(formatDate(new Date(), "dd/MM/yyyy"));

    $(".info-list-filter-startDate", this.container).datepicker({format: 'dd/mm/yyyy', weekStart: 1});
    $(".info-list-filter-endDate", this.container).datepicker({format: 'dd/mm/yyyy', weekStart: 1});
    $(".info-list-filter-startTime", this.container).timepicker({'template': 'dropdown', 'minuteStep': 15, 'showMeridian': false, 'defaultTime': 'value'});
    $(".info-list-filter-endTime", this.container).timepicker({'template': 'dropdown', 'minuteStep': 15, 'showMeridian': false, 'defaultTime': 'value'});

    $(".info-list-filter-selection", this.container).bind('change', function()
    {
        if($(".info-list-filter-selection", this.container).val() === "none")
        {
            this.loadEvents();
            $(".info-list-filter-content", this.container).slideUp(200);
        }
        else
        {
         $(".info-list-filter-content", this.container).slideDown(200);
        }
    }.bind(this));

    $(".info-list-filter-button", this.container).bind('click', this.loadEvents.bind(this));
};

/**
* Set the focused place. Clear the current informations, set the place and load its events.
*
* @method setPlace
* @param {Object} place New focused place
*/
InfoList.prototype.setPlace = function(place)
{
    this.clear();
    this.hide();
    this.place = place;
    this.loadEvents();
};

/**
* Load events of the current focused place
*
* @method loadEvents
*/
InfoList.prototype.loadEvents = function()
{
    this.clear();

    var obj = this;
    if(this.itemView === null)
    {
        $.get(jsRoutes.info.list.view.item(), function(data){
            this.itemView = data;
            this.loadEvents();
        }.bind(this));
        
        return;
    }

    var filter = $(".info-list-filter-selection", this.container).val();
    var strStartDate = $(".info-list-filter-startDate", this.container).val() + " " + $(".info-list-filter-startTime", this.container).val();
    var strEndDate = $(".info-list-filter-endDate", this.container).val() + " " + $(".info-list-filter-endTime", this.container).val();
    var startDate = new Date(getDateFromFormat(strStartDate, "dd/MM/yyyy HH:mm"));
    var endDate   = new Date(getDateFromFormat(strEndDate, "dd/MM/yyyy HH:mm"));

    var startTime = -1;
    var endTime = -1;

    if(filter !== "none"){
        if(startDate.getTime() > 0)
        {
            startTime = startDate.getTime();
        }
        if(endDate.getTime() > 0)
        {
            endTime = endDate.getTime();
        }
    }
    
    showLoader(this.container);
    $.get(jsRoutes.getPlaceEvents({'idPlace': this.place.id, 'filter': filter, 'startDate': startTime, 'endDate': endTime}), function(data){
        hideLoader(this.container);
        if(data.length > 0)
        {
            var filters = $(".info-list-filters-container", this.container);
            $(".info-list-no-info", this.container).hide();
            $(".event-list-table table", this.container).show();

            jQuery.each(data, function(i, event)
            {
                obj.addEvent(this);
            });
        }
        else
        {
            $(".info-list-no-info", this.container).show();
            $(".event-list-table table", this.container).hide();
        }
    }.bind(this)).error(function(data){
        // TODO: handle errors
    });
};

/**
* Clear data structures and view
*
* @method clear
*/
InfoList.prototype.clear = function()
{
    var oldSelection = this.selectedEvent;
    this.events = {};
    this.contents = {};
    this.selectedEvent = null;
    $(".event-list-table .event-list-body", this.container).html("");
    this.fireEvent("infoUnselected", oldSelection);
};

/**
* Add a place event to the view and datastructures. Initialize event item's handler.
*
* @method addEvent
* @param {Object} event Place event that must be added
*/
InfoList.prototype.addEvent = function(event){
  if(typeof this.events[event.id] == "undefined")
  {
      var item = $(this.itemView);
      this.fillItem(item, event);
      $(".event-list-table .event-list-body", this.container).append(item);
      
      this.contents[event.id] = item;
      this.events[event.id] = event;

      $(item).bind('click', function(){
        this.onInfoSelected(this.events[event.id]);
      }.bind(this));
  }
};

/**
* Update an added place event (called when an event is edited to refresh its view)
*
* @method updateEvent
* @param {Object} event Updated event
*/
InfoList.prototype.updateEvent = function(event)
{
    if(typeof this.events[event.id] != "undefined")
    {
        var html = this.contents[event.id];
        this.fillItem(html, event);
        this.events[event.id] = event;
        this.contents[event.id] = html;

        if(event.id == this.selectedEvent.id)
            this.selectedEvent = event;
    }
};

/**
* Fill an item template with event informations
*
* @method fillItem
* @param {DOM} item Dom object representing the item
* @param {Object} event Place event associated to the item
*/
InfoList.prototype.fillItem = function(item, event)
{
    $(".info-name", item).html(event.title);
    $(".info-type", item).html($("<span class='label' style='background:#"+event.mainTag.color+";'>"+event.mainTag.name+"</span>"));
    
    $(".info-startDate", item).html(event.startDate);
    $(".info-endDate", item).html(event.endDate);
};

/**
* Displays the event list view
*
* @method show
*/
InfoList.prototype.show = function()
{
    this.initGUI();
    this.initGUIHandlers();
    this.container.show();
};

/**
* Hide the event list view
*
* @method hide
*/
InfoList.prototype.hide = function()
{
    this.container.hide();
};

/**
* Handler when an event is deleted. Remove the event informations from datastructures and from the view
*
* @method onInfoDeleted
* @param{Object} event Deleted place event
*/
InfoList.prototype.onInfoDeleted = function(event)
{
    if(this.selectedEvent == event)
    {
        this.selectedEvent = null;
    }

    var html = this.contents[event.id];
    if(typeof html != "undefined")
    {
        html.remove();
        delete this.contents[event.id];
    }

    $("#info-list-edit-button").attr("disabled", "disabled");
    $("#info-list-delete-button").attr("disabled", "disabled");
    this.fireEvent("infoUnselected", event);
};

/**
* Handler when an event's item view is clicked. Highlight the event's item view and fire an event indicating that the event has been selected.
*
* @method onInfoSelected
* @param {Object} event Selected place Event
*/
InfoList.prototype.onInfoSelected = function(event)
{
    this.selectedEvent = event;
    var row = this.contents[event.id];
    $(".info-list-item.selected", this.container).removeClass("selected");
    row.addClass("selected");

    $("#info-list-edit-button").removeAttr("disabled");
    $("#info-list-delete-button").removeAttr("disabled");
    this.fireEvent("infoSelected", event);
};