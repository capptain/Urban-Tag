"use strict";

function CanFireEvents(eventTypes)
{
    this.listeners = {};
    
    for(var i = 0; i < eventTypes.length; i++)
    {
        var type = eventTypes[i];
        this.listeners[type] = new Array();
    };
}

CanFireEvents.prototype.register = function(eventType, action)
{
    if(typeof this.listeners[eventType] != "undefined")
    {
        this.listeners[eventType].push(action);
        return new EventRegistration(this, eventType, action);
    }
};

CanFireEvents.prototype.unregister = function(eventType, handler)
{           
    var found = false;
    var handlers = this.listeners[eventType];
    var cptHandler = 0;
    while(!found && cptHandler < handlers.length)
    {
        found = handlers[cptHandler] == handler;
        
        if(found)
        {
            if(handlers.length > 1)
            {
                for(var i = handlers.length - 1; i > cptHandlers; i--)
                {
                    this.listeners[eventType][i-1] = handlers[i];
                }
            }
            else
            {
                this.listeners[eventType] = new Array();
            }
        }
    }
};

/**
 * Fire an event of the given name with the given param
 */
CanFireEvents.prototype.fireEvent = function(eventType, param)
{
    for(var i = 0; i < this.listeners[eventType].length; i++)
    {
        var handler = this.listeners[eventType][i];
        handler(param);
    }
};