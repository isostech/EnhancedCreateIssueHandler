AJS.$(function() {
    var knownServiceProviders = {
        "gmail-smtp" : { protocol: "smtps", serverName: "smtp.gmail.com", port: "465"},
        "gmail-pop3" : { protocol: "pop3s", serverName: "pop.gmail.com", port: "995" },
        "gmail-imap" : { protocol: "imaps", serverName: "imap.gmail.com", port: "993" },

        "yahoo-smtp" : { protocol: "smtps", serverName: "smtp.mail.yahoo.com", port: "465" },
        "yahooplus-smtp" : { protocol: "smtps", serverName: "plus.smtp.mail.yahoo.com", port: "465" },
        "yahooplus" : { protocol: "pop3s", serverName: "plus.pop.mail.yahoo.com", port: "995" },

        "hotmail-smtp" : { protocol: "smtp", serverName: "smtp.live.com", port: "587" },
        "hotmail" : { protocol: "pop3s", serverName: "pop3.live.com", port: "995" },
        "aol-smtp" : { protocol: "smtp", serverName: "smtp.aol.com", port: "587" },
        "aol" : { protocol: "imap", serverName: "imap.aol.com", port: "143" }
    };

    AJS.$("select[name=serviceProvider]").change(function() {
        var val = AJS.$(this).val();
        var showFields = false;
        var protocol = AJS.$("select[name=protocol]"), serverName = AJS.$("input[name=serverName]"),
                port = AJS.$("input[name=port]"), tls = AJS.$("input[name=tlsRequired]");
        var provider = knownServiceProviders[val];
        if (provider) {
            protocol.val(provider.protocol);
            serverName.val(provider.serverName);
            port.val(provider.port);
        } else {
            showFields = true;
        }

        protocol.parent(".field-group")
                .add(protocol.parent("td.fieldValueArea").parent("tr")).toggle(showFields);
        serverName.parent(".field-group")
                .add(serverName.parent("td.fieldValueArea").parent("tr")).toggle(showFields);
        port.parent(".field-group")
                .add(port.parent("td.fieldValueArea").parent("tr")).toggle(showFields);
        tls && tls.parent(".field-group").
                add(tls.parent("td.fieldValueArea").parent("tr")).toggle(showFields);
    }).change();
});