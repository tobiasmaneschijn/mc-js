

![Minecraft JS](./blockbench/images/minecraft_title.png)


DISCLAIMER: MinePuter.JS is currently in very early development. Many features described here are planned but not yet implemented. The mod is rapidly evolving, and significant changes should be expected. Use in your world at your own risk, and please report any issues you encounter. Documentation on implemented features will be coming soon.


## Overview
MinePuter.JS is a W.I.P. Minecraft mod that introduces fully functional, programmable computers into the survival game world. These computers run JavaScript, allowing players to create and execute scripts within Minecraft, enhancing your survival experience with advanced programming capabilities. With the upcoming JavaScript-based operating system, MinePuter.JS will essentially function as a personal computer within Minecraft!

## Features
- Adds a craftable Computer Block to Minecraft survival mode
- In-game JavaScript editor and terminal
- Execute JavaScript code within Minecraft
- Interact with game data using JavaScript
- Syntax highlighting for easier coding
- Upcoming: Full JavaScript-based operating system

## Upcoming Feature: JS-based Operating System
We're excited to announce that we're developing a complete JavaScript-based operating system for MinePuter.JS. This will transform your in-game computer from a simple code execution environment into a full-fledged personal computer within Minecraft. Features will include:

- File system for storing and organizing your scripts and data
- Multitasking capabilities to run multiple scripts simultaneously
- User interface with windows, menus, and desktop metaphor
- Networking capabilities for inter-computer communication
- And much more!

Stay tuned for updates on this groundbreaking feature!

## Installation
1. Ensure you have Minecraft with NeoForge installed (Minecraft version 1.21)
2. Download the latest version of MinePuter.JS and MinePuter.JS GraalVM library the  from [mod distribution site]
3. Place both the MinePuter.JS .jar file AND the GraalVM library .jar file in your Minecraft mods folder
   - It's crucial to include both files for the mod to function correctly
4. Launch Minecraft and enjoy your new JavaScript-powered computers in survival mode!

Note: The GraalVM library is required for the JavaScript execution within Minecraft.

## Usage
1. Craft a Computer Block using the new recipe added by the mod
2. Place the Computer Block in your survival world
3. Right-click the Computer Block to open the computer interface
4. Use the built-in editor to write your JavaScript code
5. Execute your code using the "Run" button
6. View output in the terminal window

## JavaScript API
MinePuter.JS will provide several Minecraft-specific functions you can use in your scripts to interact with game data:

- `getPlayerPosition()`: Returns the player's current coordinates
- `getInventoryContents()`: Lists items in the player's inventory
- `getTime()`: Returns the current game time
- `sendMessage(text)`: Sends a chat message to the player

For a full list of available functions, please refer to our [API Documentation](link-to-api-docs).

## Examples
Here's a simple script to display the player's current position:

```javascript
let pos = getPlayerPosition();
sendMessage(`You are at X: ${pos.x}, Y: ${pos.y}, Z: ${pos.z}`);
```

## Survival Mode Integration
MinePuter.JS is designed to enhance your survival gameplay. Use it to:
- Track resources and automate inventory management
- Create custom HUDs or information displays
- Develop complex automation systems using redstone and JavaScript
- Enhance your builds with programmable features
- Run a personal computer for various in-game tasks (with upcoming OS feature)

## Personal Computing in Minecraft
With the upcoming JavaScript-based OS, you'll be able to use MinePuter.JS for a wide range of personal computing tasks within Minecraft:

- Write and run complex programs
- Manage your in-game data with a file system
- Potentially browse a simulated internet within Minecraft
- Create and use productivity tools for your Minecraft projects
- Develop mini-games or other interactive experiences

The possibilities are endless! MinePuter.JS aims to bring the power of personal computing into your Minecraft world.

## Support
If you encounter any issues or have questions, please file an issue on our [GitHub Issues page](link-to-github-issues).


## Credits
MinePuter.JS is created and maintained by Tobias Maneschijn and Magnus Boll Jensen.

This mod uses GraalVM, which is licensed under [GFTC](https://www.oracle.com/downloads/licenses/graal-free-license.html)
