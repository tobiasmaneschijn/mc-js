console.log("Bootloader starting...");

// ... (keep the existing file system initialization code) ...

// Check if the OS core exists, if not, create it
if (!fs.exists('/sys/os.js')) {
    console.log("Creating OS core...");
    const osCore = `
console.log("OS core loaded and executing...");

function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

let currentDir = '/';

function changeDir(dir) {
    if (dir.startsWith('/')) {
        currentDir = dir;
    } else {
        currentDir += '/' + dir;
    }

    // Normalize the directory path (remove '..', '.', and duplicate slashes)
    let parts = currentDir.split('/').filter(part => part && part !== '.');
    let normalizedParts = [];
    for (let part of parts) {
        if (part === '..') {
            normalizedParts.pop();
        } else {
            normalizedParts.push(part);
        }
    }
    currentDir = '/' + normalizedParts.join('/');
    console.log("Current directory: " + currentDir);
}

async function processCommand(input) {
    let commands = input.split('|').map(cmd => cmd.trim());
    let output = '';

    for (let i = 0; i < commands.length; i++) {
        let [command, ...args] = commands[i].split(' ');
        let commandInput = i === 0 ? '' : output;
        output = await executeCommand(command, args, commandInput);
        if (output === false) {
            return false;
        }
    }
    if (output !== undefined && output !== null && output.trim() !== '') {
        console.log(output);
    }
    return true;
}

async function executeCommand(command, args, input) {
    switch (command) {
        case 'ls':
            let lsDir = args[0] ? args[0] : currentDir;
            if (!lsDir.startsWith('/')) {
                lsDir = currentDir + '/' + lsDir;
            }
            let files = fs.listFiles(lsDir);
            return files.join('\\n');

        case 'cat':
            let catFile = args[0];
            if (catFile && !catFile.startsWith('/')) {
                catFile = currentDir + '/' + catFile;
            }
            return fs.readFile(catFile);

        case 'echo':
            return args.join(' ');

        case 'grep':
            if (!input) {
                console.error("No input provided to grep");
                return '';
            }
            let searchTerm = args[0];
            return input.split('\\n').filter(line => line.includes(searchTerm)).join('\\n');

        case 'help':
            return 'Available commands: ls, cat, run, help, exit, clear, cd, echo, mkdir, rmdir, rm, touch, cp, mv, pwd, grep';

        case 'exit':
            console.log("Exiting shell");
            return false;

        case 'clear':
            system.clearTerminal();
            return '';

        case 'cd':
            changeDir(args[0]);
            return '';

        case 'pwd':
            return currentDir;

        case 'mkdir':
            let mkdirPath = args[0];
            if (mkdirPath && !mkdirPath.startsWith('/')) {
                mkdirPath = currentDir + '/' + mkdirPath;
            }
            fs.createDirectory(mkdirPath);
            return "Directory created: " + mkdirPath;

        case 'rmdir':
            let rmdirPath = args[0];
            if (rmdirPath && !rmdirPath.startsWith('/')) {
                rmdirPath = currentDir + '/' + rmdirPath;
            }
            fs.deleteDirectory(rmdirPath);
            return "Directory removed: " + rmdirPath;

        case 'rm':
            let rmPath = args[0];
            if (rmPath && !rmPath.startsWith('/')) {
                rmPath = currentDir + '/' + rmPath;
            }
            fs.deleteFile(rmPath);
            return "File removed: " + rmPath;

        case 'touch':
            let touchPath = args[0];
            if (touchPath && !touchPath.startsWith('/')) {
                touchPath = currentDir + '/' + touchPath;
            }
            fs.createFile(touchPath, "");
            return "File created: " + touchPath;

        case 'cp':
            let srcPath = args[0];
            let destPath = args[1];
            if (srcPath && !srcPath.startsWith('/')) {
                srcPath = currentDir + '/' + srcPath;
            }
            if (destPath && !destPath.startsWith('/')) {
                destPath = currentDir + '/' + destPath;
            }
            let content = fs.readFile(srcPath);
            fs.writeFile(destPath, content);
            return "Copied from " + srcPath + " to " + destPath;

        case 'mv':
            let mvSrcPath = args[0];
            let mvDestPath = args[1];
            if (mvSrcPath && !mvSrcPath.startsWith('/')) {
                mvSrcPath = currentDir + '/' + mvSrcPath;
            }
            if (mvDestPath && !mvDestPath.startsWith('/')) {
                mvDestPath = currentDir + '/' + mvDestPath;
            }
            let mvContent = fs.readFile(mvSrcPath);
            fs.writeFile(mvDestPath, mvContent);
            fs.deleteFile(mvSrcPath);
            return "Moved from " + mvSrcPath + " to " + mvDestPath;

        default:
            return 'Unknown command: ' + command + '. Type "help" for available commands.';
    }
}

async function shell() {
    console.log("Shell function started");
    console.log('Welcome to JavaScript OS');

    while (true) {
        try {
            // Add a small delay to prevent high CPU usage
            await delay(100);

            let input = await system.readLine(currentDir + '> ');
            if (input.trim() !== '') {
                if (!await processCommand(input)) {
                    break;
                }
                // Clear the input after processing the command
                system.clearInput();
            }
        } catch (error) {
            console.error("Error in shell: " + error);
        }
    }
}

// Start the shell
shell().catch(error => console.error("Shell error: " + error));
    `;

    try {
        fs.writeFile('/sys/os.js', osCore);
        console.log("OS core created successfully.");
    } catch (error) {
        console.error("Error creating OS core: " + error);
    }
} else {
    console.log("OS core already exists.");
}

console.log("Loading OS core...");
try {
    const osCore = fs.readFile('/sys/os.js');
    console.log("OS core loaded. Executing...");
    eval(osCore);
} catch (error) {
    console.error("Error loading or executing OS core: " + error);
}