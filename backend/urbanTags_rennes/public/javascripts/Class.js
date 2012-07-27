// "use strict";

/**
* Perform a java-like class extension
*
* @method extend
* @param {Function} destination Destination class constructor (class which extends the other)
* @param {Function} source Source class constructor (extended class)
*/
function extend(destination, source)
{
    for (var element in source)
    {
        destination[element] = source[element];
    }
}