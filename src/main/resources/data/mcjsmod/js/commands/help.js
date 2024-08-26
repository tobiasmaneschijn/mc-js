function help(args) {
    /**
     * using getCommands()
     * {"help": {name: "help", description: "Shows a list of commands"}}
     */

        // Retrieve the JSON string and parse it into an object
    const commandsJson = getCommands();
    const commands = JSON.parse(commandsJson);

    // Nicely formatted output
    console.log("Available Commands:");

    for (let command in commands) {
        const cmd = commands[command];
        console.log(`/${cmd.name} - ${cmd.description}`);
    }

    console.log("\nIf you write anything that doesn't exist, it will be evaluated as a JavaScript expression.");

    console.log("Have a nice day!");
    return ""
}