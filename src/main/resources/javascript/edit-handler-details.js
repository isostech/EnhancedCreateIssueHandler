function editHandlerDetailsBackbone(details, availableProjects, availableBulkOptions) {
    var HandlerSettings = Backbone.Model.extend();

    var Project = Backbone.Model.extend();
    var Projects = Backbone.Collection.extend({
        model: Project
    });

    var IssueType = Backbone.Model.extend();
    var IssueTypes = Backbone.Collection.extend({
        model: IssueType
    });

    var BulkOption = Backbone.Model.extend();
    var BulkOptions = Backbone.Collection.extend({
        model: BulkOption
    });

    var CheckboxView = Backbone.View.extend({
        events: {
            "change": "changeSelected"
        },
        initialize: function() {
            _.bindAll(this, "render");
        },
        changeSelected: function() {
            var ajs = AJS.$(this.el);
            var obj = new Object();
            obj[ajs.attr("name")] = ajs.prop("checked");
            this.model.set(obj);
        },
        render: function() {
            var ajs = AJS.$(this.el), val = this.model.get(ajs.attr("name"));
            if (val === true) {
                ajs.attr("checked", "checked");
            }
            return this;
        }
    });

    var TextView = Backbone.View.extend({
        events: {
            "change": "changeSelected",
            "blur": "changeSelected"
        },
        initialize: function() {
            _.bindAll(this, "render");
        },
        changeSelected: function() {
            var ajs = AJS.$(this.el);
            var obj = new Object();
            obj[ajs.attr("name")] = ajs.val();
            this.model.set(obj);
        },
        render: function() {
            var ajs = AJS.$(this.el), val = this.model.get(ajs.attr("name"));
            if (val !== null) {
                ajs.val(val);
            }
            return this;
        }
    });

    var DefaultReporterView = TextView.extend({
        initialize: function() {
            _.bindAll(this, "render");
            this.model.bind('change', this.render, this);
            this.container = AJS.$('.field-group.reporterusername');
        },
        render: function() {
            TextView.prototype.render.call(this); // super()
            var isDisabled = this.model.get("createusers");
            this.container.toggle(!isDisabled);
            return this;
        }
    });


    var OptionView = Backbone.View.extend({
        tagName: "option",
        initialize: function() {
            _.bindAll(this, "render");
        },
        render: function() {
            var val = this.model.get('key') || this.model.get('id');
            AJS.$(this.el).attr('value', val).html(this.model.escape('name'));
            return this;
        }
    });

    var OptionsView = Backbone.View.extend({
        events: {
            "change": "changeSelected"
        },
        initialize: function() {
            _.bindAll(this, 'addOne', 'addAll');
            this.collection.bind('reset', this.addAll);
            this.addAll();
        },
        addOne: function(project) {
            var optionView = new OptionView({model: project});
            this.optionViews.push(optionView);
            AJS.$(this.el).append(optionView.render().el);
        },
        addAll: function() {
            _.each(this.optionViews, function(optionView) { optionView.remove(); });
            this.optionViews = [];
            this.collection.each(this.addOne);
            this.render();
        },
        changeSelected: function() {
            this.setSelectedId(AJS.$(this.el).val());
        }
    });

    var ProjectsView = OptionsView.extend({
        setSelectedId: function(projectKey) {
            var selectedProject = this.collection.find(function(project) { return project.get("key") == projectKey; });
            this.model.set({"projectKey": selectedProject.get("key")});
            this.issueTypesView.collection.reset(selectedProject.get("issueTypes"));
            this.issueTypesView.el.trigger("change");
        },
        render: function() {
            var val = this.model.get("projectKey");
            if (val) {
                AJS.$(this.el).val(val);
            }
            return this;
        }
    });

    var IssueTypesView = OptionsView.extend({
        setSelectedId: function(issueTypeId) {
            var selectedType = this.collection.find(function(type) { return type.get("id") == issueTypeId; });
            this.model.set({"issueTypeId": selectedType.get("id")});
        },
        render: function() {
            var val = this.model.get("issueTypeId");
            if (val) {
                AJS.$(this.el).val(val);
            }
            return this;
        }
    });

    var BulkOptionsView = OptionsView.extend({
        setSelectedId: function(optionId) {
            var selectedOption = this.collection.find(function(type) { return type.get("id") == optionId; });
            this.model.set({"bulk": selectedOption.get("id")});
        },
        render: function() {
            var val = this.model.get("bulk");
            if (val) {
                AJS.$(this.el).val(val);
            }
            return this;
        }
    });

    var SettingsView = Backbone.View.extend({
        events: {
            "click #testButton": "testHandler",
            "submit": "saveHandler"
        },
        initialize: function() {
            _.bindAll(this, "testHandler", "saveHandler", "testResultsInlineContent", "testCompleteHandler");
            this.model.bind("change", this.modelChanged, this);
        },
        render: function() {
            if (AJS.$("#project").length > 0) {
                this.projectsView = new ProjectsView({el: AJS.$("#project"), model: this.model, collection: new Projects(availableProjects)}).render();
                this.issueTypesView = new IssueTypesView({el: AJS.$("#issuetype"), model: this.model, collection: new IssueTypes()}).render();
                this.projectsView.issueTypesView = this.issueTypesView;
            }

            this.createusersView = new CheckboxView({el: AJS.$("#createusers"), model: this.model}).render();
            this.notifyusersView = new CheckboxView({el: AJS.$("#notifyusers"), model: this.model}).render();
            this.reporterView = new DefaultReporterView({el: AJS.$("#reporterusername"), model: this.model}).render();
            this.catchemailView = new TextView({el: AJS.$("#catchemail"), model: this.model}).render();
            this.forwardEmailView = new TextView({el: AJS.$("#forwardEmail"), model: this.model}).render();

            if (AJS.$("#stripquotes").length > 0) {
                this.stripquotes = new CheckboxView({el: AJS.$("#stripquotes"), model: this.model}).render();
            }

            if (AJS.$("#splitregex").length > 0) {
                this.splitregexView = new TextView({el: AJS.$("#splitregex"), model: this.model}).render();
            }

            if (AJS.$("#ccwatcher").length > 0) {
                this.ccwatcherView = new CheckboxView({el: AJS.$("#ccwatcher"), model: this.model}).render();
                this.ccassigneeView = new CheckboxView({el: AJS.$("#ccassignee"), model: this.model}).render();
            }

            this.bulkOptionsView = new BulkOptionsView({el: AJS.$("#bulk"), model: this.model, collection: new BulkOptions(availableBulkOptions)}).render();

            this.modelChanged();
            return this;
        },
        getModelAsString: function() {
            var modelJson = this.model.toJSON();
            if (this.model.get("createusers")) {
                // we don't want to send it to the server - as it will cause the validation to fail
                // let's keep in locally in the model and only when the customer actually saves this
                // then we will lose the username potentially provided in UI.
                // let's respect customer input :)
                delete modelJson["reporterusername"];
            }
            return JSON.stringify(modelJson);
        },
        testResultsInlineContent: function(contents, trigger, show) {
            var data = this.testResult || {};
            contents.html(JIRA.Templates.Mail.testDialog({ "data": data}));
            show();
        },
        testCompleteHandler: function(xhr, textStatus, smartAjaxResult) {
            AJS.$(".buttons .throbber:last").removeClass("loading");

            if (smartAjaxResult.successful) {
                this.testResult = smartAjaxResult.data;
            } else {
                this.testResult = { succeeded: false, errors: [ JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult) ] };
            }
            if (this.testResult.succeeded) {
                if (this.testResult.stats.messages == 0) {
                    AJS.messages.info(AJS.$("#mailHandlerForm .buttons .test-placeholder"), {
                        body: AJS.I18n.getText('jmp.editHandlerDetails.testDialog.nomessages'),
                        shadowed: false,
                        closeable: false
                    });
                } else {
                    AJS.messages.success(AJS.$("#mailHandlerForm .buttons .test-placeholder"), {
                        body: AJS.I18n.getText('jmp.editHandlerDetails.testDialog.success'),
                        shadowed: false,
                        closeable: false
                    });
                }

            } else {
                AJS.messages.error(AJS.$("#mailHandlerForm .buttons .test-placeholder"), {
                    body: AJS.I18n.getText('jmp.editHandlerDetails.testDialog.error'),
                    shadowed: false,
                    closeable: false
                });
            }
            AJS.$("#mailHandlerForm .buttons .test-placeholder a").click({dialog: this}, function(event) {
                event.data.dialog.testResultsInlineDialog.show();
                event.preventDefault();
            });
        },
        testHandler: function(e) {
            e.preventDefault();

            var jsonData = { "detailsJson": this.getModelAsString() };

            AJS.$("#mailHandlerForm .buttons .test-placeholder .aui-message").remove();
            AJS.$(".buttons .throbber:last").addClass("loading");

            if (this.testResultsInlineDialog == undefined) {
                this.testResultsInlineDialog = new AJS.InlineDialog(
                        AJS.$("#mailHandlerForm div.buttons .test-placeholder"), "test-results-popup", this.testResultsInlineContent, {
                    width: 550,
                    onHover: false,
                    onTop: true,
                    noBind: true,
                    cacheContent: false
                });
            } else {
                this.testResultsInlineDialog.hide();
            }

            JIRA.SmartAjax.makeRequest({
                url: contextPath + "/rest/jira-mail-plugin/1.0/message-handlers/test?atl_token=" + atl_token(),
                type: "POST",
                dataType: "json",
                data: jsonData,
                complete: this.testCompleteHandler
            });
        },
        saveHandler: function() {
            AJS.$("#details").val(this.getModelAsString());
        },
        modelChanged: function() {
            AJS.$(".icon-required", AJS.$("div.field-group.forwardEmail")).toggle(this.model.get("bulk") == "forward");
            AJS.$(".hints-section").toggle(
                    this.model.get("createusers") != true
                            && (this.model.get("reporterusername") == undefined || this.model.get("reporterusername") == ""));

            AJS.$("#testButton").attr("disabled", "disabled");

            var jsonData = { "detailsJson": this.getModelAsString() };

            JIRA.SmartAjax.makeRequest({
                url: contextPath + "/rest/jira-mail-plugin/1.0/message-handlers/validate?atl_token=" + atl_token(),
                type: "POST",
                dataType: "json",
                data: jsonData,
                complete: function(xhr, textStatus, smartAjaxResult) {
                    if (smartAjaxResult.successful) {
                        var disableTest = false;
                        AJS.$(".field-group div.error").remove();
                        AJS.$("div.aui-message.error").remove();

                        if (smartAjaxResult.data.globalErrors != undefined
                                && smartAjaxResult.data.globalErrors.length > 0) {
                            disableTest = true;

                            AJS.$.each(smartAjaxResult.data.globalErrors, function(idx, value) {
                                AJS.messages.error(AJS.$(".global-errors-location"), {
                                    body: value,
                                    shadowed: false,
                                    closeable: false
                                });
                            });
                        }
                        if (AJS.$.isEmptyObject(smartAjaxResult.data.fieldErrors) == false) {
                            disableTest = true;

                            AJS.$.each(smartAjaxResult.data.fieldErrors, function(key, value) {
                                AJS.$(".field-group." + key + " span.element-wrapper").append("<div class='error'>" + value + "</div>");
                            });
                        }
                        AJS.$("#testButton").prop("disabled", disableTest);
                    } else {
                        AJS.$("#testButton").prop("disabled", true);
                    }
                }
            });
        }
    });

    var settings = new HandlerSettings(details);
    var settingsView = new SettingsView({el: AJS.$("#mailHandlerForm"), model: settings}).render();

    if (settingsView.projectsView !== undefined) {
        settingsView.projectsView.el.trigger("change");
    }
}