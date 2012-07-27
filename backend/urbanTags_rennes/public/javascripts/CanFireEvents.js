        // "use strict";

/**
* This class provides a pattern for object that can fire events
* @class CanFireEvents
* @contructor
* @param {String[]} eventTypes Types of possible fired events
*/
function CanFireEvents(eventTypes)
{
    this.listeners = {};
    
    for(var i = 0; i < eventTypes.length; i++)
    {
        var type = eventTypes[i];
        this.listeners[type] = [];
    }
}

/**
* This method associate an event type to an action.
*
* @method register
* @param {String} eventType Event type at which we associate a new action/handler
* @param {Function} action Handler associated to the event type
* @return {EventRegistration} Return the EventRegistration corresponding to the association between the event type and the handler
*/
CanFireEvents.prototype.register = function(eventType, action)
{
    if(typeof this.listeners[eventType] != "undefined")
    {
        this.listeners[eventType].push(action);
        return new EventRegistration(this, eventType, action);
    }
};

/**
* This method remove a handler associated to an event type
*
* @method unregister
* @param {String} eventType Type of event at which the handler must be associated
* @param{Function} handler Handler associated to the event type
*/
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
                this.listeners[eventType] = [];
            }
        }
    }
};

/**
 * Fire an event of the given name with the given param
 *
 * @method fireEvent
 * @param {String} eventType Type of fired event
 * @param {Object} param Object associated to the event
 */
CanFireEvents.prototype.fireEvent = function(eventType, param)
{
    for(var i = 0; i < this.listeners[eventType].length; i++)
    {
        var handler = this.listeners[eventType][i];
        handler(param);
    }
};