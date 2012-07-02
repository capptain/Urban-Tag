$(function(){  
    login = function()
    {
        $('#loginDiv .popup-content').load( jsRoutes.getLoginFormAction(),
           function() {
            $('#loginDiv').css('z-index', 10000)
            $('#loginDiv').fadeIn(300)
            $('#loginDiv .popup-main-container').center()
           }
        )
    }
    
	$('#loginButton').click(login);
});