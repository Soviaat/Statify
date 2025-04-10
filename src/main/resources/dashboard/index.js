const express = require('express');
const app = express();
const path = require('path');
const fs = require('fs');
const {text} = require("express");

// Serve static files (like CSS, JS, etc.)
app.use(express.static(path.join(__dirname, 'public')));

// Use a flag to choose between test and release mode
const testMode = true; // Set to false when releasing

// Define the base folder for CSV files
const baseFolder = testMode
    ? path.join(__dirname, '..', '..', '..', '..', 'run', 'Statify')
    : path.join(process.env.APPDATA, '.minecraft', 'Statify');

// Route for fetching stats data
app.get('/data/stats', (req, res) => {
    const world = req.query.world || 'defaultWorld';
    const statsFilePath = path.join(baseFolder, world, 'statify_stats.csv');

    console.log(`Looking for stats file at: ${statsFilePath}`);

    if (fs.existsSync(statsFilePath)) {
        const stats = readCSV(statsFilePath);
        res.json(stats);
    } else {
        res.status(404).json({ error: `Stats file not found for world: ${world}` });
    }
});

// Route for fetching days data
app.get('/data/days', (req, res) => {
    const world = req.query.world || 'defaultWorld';
    const daysFilePath = path.join(baseFolder, world, 'days.csv');

    console.log(`Looking for days file at: ${daysFilePath}`);

    if (fs.existsSync(daysFilePath)) {
        const days = readDaysFile(daysFilePath); // Use special function for days file
        res.json(days);
    } else {
        res.status(404).json({ error: `Days file not found for world: ${world}` });
    }
});

app.get('/data/player', (req, res) => {
    const world = req.query.world || 'defaultWorld';
    const playerFilePath = path.join(baseFolder, world, 'player.txt');
    console.log(`Looking for player file at: ${playerFilePath}`);

    if (fs.existsSync(playerFilePath)) {
        const player = readPlayerName(playerFilePath);
        res.json(player);
    } else {
        res.status(404).json({ error: `Player file not found for world: ${world}` });
    }
})

// Function to read and parse CSV data into JSON for stats
function readCSV(filePath) {
    try {
        const data = fs.readFileSync(filePath, 'utf8');
        // Split rows and ignore empty lines
        const rows = data.split('\n').filter(row => row.trim() !== '');
        let result = {};
        rows.forEach(row => {
            // Split the row by semicolon; e.g., "mined;minecraft:oak_sign;0"
            const cols = row.split(';');
            if (cols.length >= 3) {
                const category = cols[0].trim();    // e.g., "mined"
                const identifier = cols[1].trim();  // e.g., "minecraft:oak_sign"
                const value = parseInt(cols[2].trim(), 10); // Convert value to integer

                // Only add to result if the value is not zero
                if (value !== 0) {
                    // Create the group if it doesn't exist yet
                    if (!result[category]) {
                        result[category] = {};
                    }
                    result[category][identifier] = value;
                }
            }
        });
        return result;
    } catch (err) {
        console.error('Error reading CSV file:', err);
        return {};
    }
}

function readPlayerName(filePath) {
    try {
        const data = fs.readFileSync(filePath, 'utf8');
        return data.toString();
    } catch (err) {
        console.error('Error reading player.txt file:', err);
        return null;
    }
}

// Function to read the days file (special handling for a number)
function readDaysFile(filePath) {
    try {
        const data = fs.readFileSync(filePath, 'utf8').trim();
        return parseInt(data, 10);  // Parse the data as an integer
    } catch (err) {
        console.error('Error reading days file:', err);
        return null;
    }
}

app.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'dashboard.html'));
});

// Start the server
const port = 3000;
app.listen(port, () => {
    console.log(`Server is running at http://localhost:${port}`);
});
