// "use strict";

/**
* This class represents the registration of a handler to a specific event type of another object
*
* @class EventRegistration
* @contructor
* @param {Object} target Targeted object (the object which fires event)
* @param {String} eventName Event type
* @param {Function} handler Registered function
*/
function EventRegistration(target, eventName, handler)
{
	this.eventName = eventName;
	this.target = target;
	this.handler = handler;
}

/**
* Remove a handler registration
*
* @method removeHandler
*/
EventRegistration.prototype.removeHandler = function()
{
	this.target.unregister(this.eventName, this.handler);
};