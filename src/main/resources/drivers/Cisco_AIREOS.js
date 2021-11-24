var Info = {
        name: "CiscoAireOS",
        description: "Cisco Aireos for WLCs",
        author: "abaretta",
        version: "0.2"
};

var Config = {
    "osVersion": {
                type: "Text",
                title: "AireOS version",
                comparable: true,
                searchable: true,
                checkable: true
        },
    "configuration": {
                type: "LongText",
                title: "Running configuration",
                comparable: true,
                searchable: true,
                checkable: true,
                dump: {
                        pre: "!! Running configuration (taken on %when%):",
                        post: "!! End of running configuration"
                }
        }
};

var Device = {
};

var CLI = {
        ssh: {
                macros: {
                        user: {
                                options: [ "username","password","user" ],
                                target: "user"
                        }
                }
        },
        username: {
                prompt: /^User: $/,
                macros: {
                        auto: {
                                cmd: "$$NetshotUsername$$",
                                options: [ "password" ]
                        }
                }
        },
        password: {
                prompt: /^Password:$/,
                macros: {
                        auto: {
                                cmd: "$$NetshotPassword$$",
                                options: [ "user" ]
                        }
                }
        },
        user: {
                prompt: /^\(Cisco Controller.*\) \>$/,
                error: /^Incorrect usage.+$/m,
                pager: {
                        avoid: "config paging disable",
                        match: /^--More-- or \(q\)uit$/,
                        response: " "
               }
        }
};

function snapshot(cli, device, config) {

    cli.macro("user");

    var configuration = cli.command("show run-config startup-commands");

        var removeChangingParts = function(text) {
                var cleaned = text;
                cleaned = cleaned.replace(/^# WLC Config Begin.*$/m, "");
                cleaned = cleaned.replace(/^# WLC Config End.*$/m, "");
                return cleaned;
        }

        // If only the passwords are changing (they are hashed with a new salt at each 'show') then
        // just keep the previous configuration.
        // That means we could miss a password change in the history of configurations, but no choice...
        var previousConfiguration = device.get("configuration");
        if (typeof previousConfiguration === "string" &&
                        removeChangingParts(previousConfiguration) === removeChangingParts(configuration)) {
                config.set("configuration", previousConfiguration);
        }
        else {
                config.set("configuration", configuration);
        }

   try {   
           var showInventory = cli.command("show inventory");
           var inventoryPattern = /NAME: \"(.*)\" +, DESCR: \"(.*)\"[\r\n]+PID: (.*?) *, +VID: (.*), +SN: (.*)$/gm;
           var match;
           while (match = inventoryPattern.exec(showInventory)) {
                   var module = {
                           slot: match[1],
                           partNumber: match[3],
                           serialNumber: match[5]
                   };
                   device.add("module", module);
                   if (module.slot.match(/^1$/) || module.slot.match(/^Switch 1$/) || module.slot.match(/[Cc]hassis/)) {
                           device.set("serialNumber", module.serialNumber);
                   }
            }
        }
        catch (e) {
                cli.debug("show inventory not supported on this device?");
        }

    var sysinfo = cli.command("show sysinfo");

    var hostname = sysinfo.match(/^System Name\.*\s(\D.*)$/m);
        if (hostname != null) {
                device.set("name", hostname[1]);
                hostname = hostname[1];
        }
        else {
                hostname = "";
        }

    var location = sysinfo.match(/^System Location\.*\s(\D.*)$/m);
        if (location != null) {
                device.set("location", location[1]);
        }
        else {
                location = "";
        }

    var contact = sysinfo.match(/^System Contact\.*\s(\D.*)$/m);
        if (contact != null) {
                device.set("contact", contact[1]);
        }
        else {
                contact = "";
        }

    var version = sysinfo.match(/^Product Version\.*\s(\d.*)$/m);
        if (version != null) {
                device.set("softwareVersion", version[1]);
                config.set("osVersion", version[1]);
        }
        else {
                version = "";
        }

    var family = sysinfo.match(/^Product Name\.*\s(\D.*)$/m);
        if (family != null) {
                device.set("family", family[1]);
        }
        else {
                family = "";
        }

    device.set("networkClass", "SWITCH");

// TODO: add interface details   
// var showInterfaceSummary = cli.command("show interface summary");
  
};
