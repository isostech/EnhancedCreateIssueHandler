AJS.$(function() {
    var fillToolTip = function (contents, trigger, showPopup) {
        contents.html(AJS.$("#obsolete-settings-message").html());
        contents.css("background", "#FFFFDD");
        contents.parent().find("#arrow-obsolete-settings-popup path").attr("fill", "#FFFFDD");
        showPopup();
    };
    AJS.InlineDialog(AJS.$(".obsolete-settings-hover"), "obsolete-settings-popup", fillToolTip, {width: 450, onHover: true, onTop: true, hideDelay: 0});

    var editInPopup = function() {
        var dialog = new JIRA.FormDialog({
            trigger: this,
            id: this.id + "-dialog",
            ajaxOptions: {
                url: this.href,
                data: {
                    decorator: "dialog",
                    inline: "true"
                }
            }
        });
    };

    AJS.$("a#add-incoming-mail-handler").each(editInPopup);
    AJS.$("#mail-handlers-table .edit").each(editInPopup);
});
