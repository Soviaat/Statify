<div align=center>

[![Static Badge](https://img.shields.io/badge/Fabric->=0.16.14-1f6feb?style=flat-square)](https://fabricmc.net/use/installer/)
![Static Badge](https://img.shields.io/badge/Minecraft-1.21.7-green?style=flat-square&logo=Minetest&logoColor=white)
</div>
<div align=center id="versions">
  
  [![Static Badge](https://img.shields.io/badge/View_Versions-red?style=flat-square)](https://github.com/Soviaat/Statify/releases/)

</div>

<h1 align=center width=20>Statify</h1>

<div align=center><img src="https://github.com/Soviaat/Statify/blob/main/imgs/rollicon-transparent.gif?raw=true" width=300></div>

<img alt="Huge thanks to Kendiii for making this mod more colorful:)" src="https://github.com/Soviaat/Statify/blob/main/imgs/thankskendi.png?raw=true">

<p align=center>This mod gets all the statistics from your Minecraft World every 2 minutes and uploads it into Google Sheets.</p>

<div id="usage">

<h2>Tutorial on: How to install</h2>

<ol>
  <li align=justify>
  
  To install this mod, press on the [View Versions](https://github.com/Soviaat/Statify#versions) button on the top of this README file.
  </li>
  <li align=justify>Click on the version you need for your instance of Minecraft, then click on the .jar file to download it.</li>
  <li align=justify>
    
After you downloaded the .jar file, press <kbd>Win</kbd>+<kbd>R</kbd> and type in ` %appdata%/.minecraft/mods ` and press enter. You should be welcomed by your mods folder.
  </li>
  <li align=justify>Put the .jar file into your mods folder.</li>
  <li align=justify>You should be good to go! :D</li>
</ol>
   
`
  Keep in mind that the version you download must be compatible with the
  Fabric and Minecraft versions you have installed in your mods folder, this is always specified under every release. 
`  
</div>

<div id="setup">

<h1>Tutorial on: How to set up Google Sheets</h1>

1. First of all, Kendiii made a boilerplate [Google Sheet](https://docs.google.com/spreadsheets/d/1nGZAkqGMEmltLfvBtCr4GKUFrnvnlBlJlPddw4Wj6sc/edit?usp=sharing) for y'all that you can easily make a copy of and rename it.
<div align="center">

<img alt="File - Make a copy" src="https://github.com/Soviaat/Statify/blob/main/imgs/makeacopy.png?raw=true" width=300>
  
</div>



2. Then, you should be able to see your copy of this Boilerplate Sheet. On the top, you will see a yellow ribbon, click "Allow access". This will allow you to see the pictures of the items on the Statistics and Analysis sheets. 

<img alt="Yellow warning ribbon" src="https://github.com/Soviaat/Statify/blob/main/imgs/yellowribbon.png?raw=true">

3. After you pressed "Allow access", you need to give permission to the Mod's Worker Account.

<table>
  <tr>
    <td>Step 1: On the top right, click on "Share"</td>
    <td>
      <img alt="Share button on the top right" src="https://github.com/Soviaat/Statify/blob/main/imgs/share.png?raw=true">
    </td>
  </tr>
  <tr>
    <td>
      Step 2: Paste the Worker Account's e-mail into the textbox and set the permission level to <b>Editor</b><br>
      (Worker account: <code>statify@skilled-boulder-374617.iam.gserviceaccount.com</code>)<br>
      Then click <kbd width=20>Share</kbd>
    </td>
    <td>
      <img alt="Step 1 - Paste into textbox | Step 2 - Permission: Editor | Step 3: Click 'Share'" src="https://github.com/Soviaat/Statify/blob/main/imgs/editoraccess.png?raw=true">
    </td>
  </tr>
  <tr>
    <td colspan=2>Don't worry, this Worker Account will not be able to access any of your personal information, and does not inject any harmful code into your Sheets. Only your statistics will be uploaded.</td>
  </tr>
</table>

4. After you gave permission to the Worker Account for your sheet, you'll need to copy the Sheet's ID from the URL bar at the top. This ID is found in between the `spreadsheets/d/` and `/edit` section of the URL.

![URL Bar](https://github.com/Soviaat/Statify/blob/main/imgs/sheetid.png?raw=true)

4. Now, get in-game. And type the command `/statify sheetid [ID]`. (Replace [ID] with your Sheet's ID) and press Enter.

<h3>After enabling the mod with <code>/statify enable</code> make sure the upload is on with <code>/statify upload on</code>, then you'll need to restart Minecraft.</h6>
<h5>So before you think the mod does not work, it does, you just have restart the game after enabling it to see your stats appearing.</h5>

</div>
