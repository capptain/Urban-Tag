"use strict";

function extend(destination, source)
{
    for (var element in source)
    {
        destination[element] = source[element];
    }
}