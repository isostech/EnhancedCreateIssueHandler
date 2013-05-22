(function ($)
{
    AJS.namespace('JIRA.app.admin.email');
    var email = JIRA.app.admin.email;

    email.dialogInitializer = function() {
        AJS.$("#verifyServer").hide();

        var changePassword = AJS.$('#changePassword');
        if (changePassword.length > 0) {
            var passwordSection = AJS.$('input[name=password]').parents('tr').first();
            var passwordChangeDescription = AJS.$('<div class="fieldDescription" />')
                    .html(AJS.I18n.getText("admin.mailservers.password.usernamechanged.description"))
                    .hide()
                    .insertAfter(changePassword);

            changePassword.change(function() {
                passwordSection.toggle(AJS.$(this).prop("checked"));
            }).change();

            var originalUsername = AJS.$("#originalUsername").val();
            AJS.$('input[name=username]').bind("change input", function () {
                if (originalUsername == AJS.$(this).val()) {
                    changePassword.removeAttr('disabled');
                    passwordChangeDescription.hide();
                } else {
                    changePassword.attr('disabled', 'disabled').attr('checked', 'checked').change();
                    passwordChangeDescription.show();
                }
            });
        }
     };

    email.verifyServerConnection = function(e, url) {
        AJS.$("#verifyServer").show();
        AJS.$("#verifyMessages").hide();
        document.forms.jiraform.action = url;
        AJS.$(document.forms.jiraform).submit();

    };

    $(email.dialogInitializer);
})(AJS.$);
