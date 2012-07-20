(function(){
    "use strict";
})();

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
    
    if(typeof place != "undefined")
    {
        this.place = place;
        this.loadEvents();
    }

    this.infoAddedRegistration = this.infoWizard.register("infoAdded", this.addEvent.bind(this));
    this.infoEditedRegistration = this.infoWizard.register("infoEdited", this.updateEvent.bind(this));
}

extend(InfoList.prototype, CanFireEvents.prototype);

InfoList.prototype.setPlace = function(place)
{
    this.place = place;
    this.loadEvents();
};

InfoList.prototype.loadEvents = function()
{
    var obj = this;
    if(this.itemView === null)
    {
        $.get(jsRoutes.info.list.view.item(), function(data){
            this.itemView = data;
            this.loadEvents();
        }.bind(this));
        
        return;
    }

    obj.show();
    
    $.get(jsRoutes.getPlaceEvents({'idPlace': this.place.id}), function(data){
        jQuery.each(data, function(i, event)
        {
            obj.addEvent(this);
        });
    }.bind(this)).error(function(data){
        
    });
};

InfoList.prototype.clear = function()
{
    this.events = {};
    this.contents = {};
    this.selectedEvent = null;
    this.hide();
};

InfoList.prototype.addEvent = function(event){
  if(typeof this.events[event.id] == "undefined")
  {
      var item = $(this.itemView);
      this.fillItem(item, event);
      $(".event-list-table .event-list-body", this.templateHtml).append(item);
      
      this.contents[event.id] = item;
      this.events[event.id] = event;

      $(item).bind('click', function(){
        this.onInfoSelected(event);
      }.bind(this));
  }
};

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

InfoList.prototype.fillItem = function(item, event)
{
    $(".info-name", item).html(event.title);
    $(".info-type", item).html($("<span class='label' style='background:#"+event.mainTag.color+";'>"+event.mainTag.name+"</span>"));
    
    $(".info-startDate", item).html(event.startDate);
    $(".info-endDate", item).html(event.endDate);
};

InfoList.prototype.show = function()
{
    if(this.templateView === null){
        $.get(jsRoutes.info.list.view.getTemplate(), function(data)
        {
            this.templateView = data;
            this.show();
        }.bind(this));
        
        return;
    }
    
    this.templateHtml = $(this.templateView);
    this.container.html(this.templateHtml);
    this.initGUIHandlers();
};

InfoList.prototype.initGUIHandlers = function()
{
    $("#info-list-add-button", this.templateHtml).bind("click", function()
    {
        this.infoWizard.startCreatingEvent(this.place);
    }.bind(this));

    $("#info-list-edit-button", this.templateHtml).bind("click", function()
    {
        this.infoWizard.startEditingEvent(this.selectedEvent);
    }.bind(this));

    $("#info-list-delete-button", this.templateHtml).bind("click", function()
    {
        $.get(jsRoutes.info.remove({'id': this.selectedEvent.id}), function(data){
            this.onInfoDeleted(this.selectedEvent);
        }.bind(this));
    }.bind(this));
};

InfoList.prototype.hide = function()
{
    this.templateHtml.remove();
};

InfoList.prototype.onInfoDeleted = function(info)
{
    if(this.selectedEvent == info)
    {
        this.selectedEvent = null;
    }

    var html = this.contents[info.id];
    if(typeof html != "undefined")
    {
        html.remove();
        delete this.contents[info.id];
    }

    $("#info-list-edit-button").attr("disabled", "disabled");
    $("#info-list-delete-button").attr("disabled", "disabled");
    this.fireEvent("infoUnselected", info);
};

InfoList.prototype.onInfoSelected = function(info)
{
    this.selectedEvent = info;
    var row = this.contents[info.id];
    $(".info-list-item.selected", this.templateHtml).removeClass("selected");
    row.addClass("selected");

    $("#info-list-edit-button").removeAttr("disabled");
    $("#info-list-delete-button").removeAttr("disabled");
    this.fireEvent("infoSelected", info);
};