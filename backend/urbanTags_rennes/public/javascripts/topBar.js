$(function(){  
    login = function()
    {
        $('#loginDiv .modal-body').load( jsRoutes.getLoginFormAction(),
           function() {
            $('#loginDiv').modal('show');
           }
        )
    }
    
	$('#loginButton').click(login);
});