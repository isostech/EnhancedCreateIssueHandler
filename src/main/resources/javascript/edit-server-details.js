var Mail = Mail || {};

Mail.EditServerDetails = function() {
    AJS.$("select#mailServer").bind("change keyup", function() {
        var optgroup = AJS.$(this).find("option:selected").parent("optgroup");
        var group = optgroup != undefined ? optgroup.attr("label") : "";

        var folderGroup = AJS.$("#folder").parents(".field-group").first();

        AJS.$("div.description", folderGroup).text(
                "IMAP" == group ? AJS.I18n.getText('admin.service.imap.folder.desc')
                        : AJS.I18n.getText('admin.services.edit.file.service.directory', "[jira.home]/import/mail"));
        folderGroup.toggle("POP3" != group);
    }).change();
}