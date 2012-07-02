"use strict";

function EventRegistration(_target, _eventName, _handler)
{
	this.eventName = _eventName;
	this.target = _target;
	this.handler = _handler;
}

EventRegistration.prototype.removeHandler = function()
{
	this.target.unregister(this.eventName, this.handler);
};