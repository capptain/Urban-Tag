jQuery.fn.center = function () {
    this.css("position","absolute");
    this.css("top", (($(window).height() - this.outerHeight()) / 2) +
                                                $(window).scrollTop() + "px");
    this.css("left", (($(window).width() - this.outerWidth()) / 2) +
                                                $(window).scrollLeft() + "px");
    return this;
};

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

function displayAlert(message, element)
{
  if(typeof element == "undefined")
  {
    element = $("#alert-container");
  }
  var container = $("<div class='alert alert-error'><button class='close' data-dismiss='alert'>×</button><div class='content'>"+message+"</div></div>");
  container.hide();
  element.append(container);
  container.fadeIn(200, function()
  {
    setTimeout(function()
    {
      container.fadeOut(200, function(){ container.remove(); });
    }, 3000);
  });
}

function showLoader(container)
{
    container.children().not(".loader").hide();
    if($(".loader", container).length === 0){
      container.append("<div class='loader'></div>");
    }

    $(".loader", container).addClass("show");
}

function hideLoader(container)
{
  $(".loader", container).removeClass("show");
    container.children().not(".loader").show();
}

var stopAnimations = function(){};

function focusOnTop()
{
  stopAnimations();
  resizePanel($("#map"), 500);
}

function focusOnDescription()
{
  stopAnimations();
  resizePanel($("#map"), 50);
  
}

var resizePanel = function(element, height)
{
    element.css("height", height+"px");
};

// If IE
if($.browser.msie)
{
  stopAnimations = function()
  {
    $("#map").stop();
    $("#place-sheet-container").stop();
  };

  resizePanel = function(element, height, callback)
  {
    var handlers = element.data("events");
    element.animate({ "height": height+"px"}, 'fast', 'linear');
  };
}