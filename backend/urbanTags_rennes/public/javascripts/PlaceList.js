"use strict";

function PlaceList(_container, _manager) {
    this.container = _container;
    this.manager = _manager;
    
    this.placeAddedRegistration = this.manager.addPlaceAddedListener(this.onPlaceAdded.bind(this));
    this.placeEditedRegistration = this.manager.addPlaceSelectedListener(this.onPlaceSelected.bind(this));
}

/**
 * Load places from the server and display them
 */
PlaceList.prototype.refresh = function()
{
    var obj = this;
    jQuery.each(this.manager.places, function(i, place)
    {
        obj.addPlace(this);
    });
    
    this.display();
};
    
/**
 * Display the current PlaceList's object in its container
 */
PlaceList.prototype.display = function()
{            
    var table = "<table><tr class='title'><th>Name</th><th>Actions</th></tr>";
    jQuery.each(this.manager.places, function(i, place)
    {
        var subclass = (i%2 === 0)?'':'alt';
        table += "<tr class='place";
        if(subclass !== '')
        {
            table += " " + subclass;
        }
        
        table += "'>";
        
        table += "<td>" + place.name + "</td><td></td></tr>";
    });    
    
    table += "</table>";
    
    this.container.html(table);
    
    var obj = this;
    $(this.container).find('tr.place').click(function(){
        var row = $(this).parent().children("tr.place").index($(this));
        obj.manager.selectPlace(row);
    });
};

/**
 * Refresh the list's display
 */
PlaceList.prototype.onPlaceAdded = function(place)
{
    this.display();
};

/**
 * Change the background of the row corresponding to the selected place 
 */
PlaceList.prototype.onPlaceSelected = function(place)
{
    var selectedIndex = this.manager.places.indexOf(place);
    if(selectedIndex !== -1)
    {
        jQuery('tr.selected', this.container).removeClass('selected');
        jQuery('tr.place:eq(' + selectedIndex + ')', this.container).addClass('selected');
    }
};