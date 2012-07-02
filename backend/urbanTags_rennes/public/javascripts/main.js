jQuery.fn.center = function () {
    this.css("position","absolute");
    this.css("top", (($(window).height() - this.outerHeight()) / 2) + 
                                                $(window).scrollTop() + "px");
    this.css("left", (($(window).width() - this.outerWidth()) / 2) + 
                                                $(window).scrollLeft() + "px");
    return this;
}

Object.size = function(arr) 
{
    var size = 0;
    for (var key in arr) 
    {
        if (arr.hasOwnProperty(key)) size++;
    }
    return size;
};

function isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }

messages = {};

function getMessage(messageName, callback)
{
    if(typeof messages[messageName] == "undefined")
    {
        $.get(jsRoutes.messages.getMessage({'messageName': messageName}),
            function(data)
            {
                messages[messageName] = data;
                
                if(typeof callback != "undefined")
                {
                    callback(data);
                }
            }
        );
    }
    else if(typeof callback != "undefined")
    {
        callback(messages[messageName]);
    }
}