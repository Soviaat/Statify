<div align=center>

![Static Badge](https://img.shields.io/badge/Fabric->=0.16.5-1f6feb?style=flat-square&link=https://fabricmc.net/use/installer/)
![Static Badge](https://img.shields.io/badge/Minecraft-1.21.1-green?style=flat-square&logo=Minetest&logoColor=white)
  
</div>


<h1 align=center>Statify</h1>
<img alt="Huge thanks to Kendiii for making this mod more colorful:)" src="https://cdn.discordapp.com/attachments/961185822730764298/1289953406261268641/image.png?ex=66fab23a&is=66f960ba&hm=86da77a02809a6695092c578b004832ac075000ec0d9237fcd46e3ed5c76706c&">

<p align=center>This mod gets all the statistics from your Minecraft World every 2 minutes and uploads it into Google Sheets.</p>


<div id="setup">

<h1>Tutorial on: How to set up Google Sheets</h1>

1. First of all, Kendiii made a boilerplate [Google Sheet](https://docs.google.com/spreadsheets/d/1nGZAkqGMEmltLfvBtCr4GKUFrnvnlBlJlPddw4Wj6sc/edit?usp=sharing) for y'all that you can easily make a copy of and rename it.
<div align="center">

<img alt="File - Make a copy" src="https://cdn.discordapp.com/attachments/961185822730764298/1289921706181525606/image.png?ex=66fa94b4&is=66f94334&hm=69830e631ea64a0fce2106c4982f86f7277b27723b8bb9dc039abf861dfa73e8&" width=300>
  
</div>



2. Then, you should be able to see your copy of this Boilerplate Sheet. On the top, you will see a yellow ribbon, click "Allow access". This will allow you to see the pictures of the items on the Statistics and Analysis sheets. 

<img alt="Yellow warning ribbon" src="https://cdn.discordapp.com/attachments/961185822730764298/1289925474868400128/image.png?ex=66fa9836&is=66f946b6&hm=384a6d74e54410d8a0fe2e599bd5757dff580422f9215c0eeb4f8692ac3d95aa&">

3. After you pressed "Allow access", you need to give permission to the Mod's Worker Account.

<table>
  <tr>
    <td>Step 1: On the top right, press on "Share"</td>
    <td>
      <img alt="Share button on the top right" src="https://cdn.discordapp.com/attachments/961185822730764298/1289928296565575701/image.png?ex=66fa9ad7&is=66f94957&hm=e3c8081433e047df58be652d449da47ebba6762b686dc16bc8f80a64da62a758&">
    </td>
  </tr>
  <tr>
    <td>
      Step 2: Paste the Worker Account's e-mail into the textbox and set the permission level to <b>Editor</b><br>
      (Worker account: <code>statify@skilled-boulder-374617.iam.gserviceaccount.com</code>)<br>
      Then press <kbd width=20>Share</kbd>
    </td>
    <td>
      <img alt="Step 1 - Paste into textbox | Step 2 - Permission: Editor | Step 3: Press 'Share'" src="https://cdn.discordapp.com/attachments/961185822730764298/1289935026385063976/image.png?ex=66faa11c&is=66f94f9c&hm=6997ef72ed09829bcafc1d49d266a7057908ccc84c1ee83338fa3e1ed4306b5c&">
    </td>
  </tr>
  <tr>
    <td colspan=2>Don't worry, this Worker Account will not be able to access any of your personal information, and does not inject any harmful code into your Sheets. Only your statistics will be uploaded.</td>
  </tr>
</table>

4. After you gave permission to the Worker Account for your sheet, you'll need to copy the Sheet's ID from the URL bar at the top. This ID is found in between the `spreadsheets/d/` and `/edit` section of the URL.

![URL Bar](https://cdn.discordapp.com/attachments/961185822730764298/1289926305860550821/image.png?ex=66fa98fc&is=66f9477c&hm=72f305a77030dc77ed4b9a2376456de715c3f0bc4d204c9c9a2c66876bdb6f70&)

4. Now, get in-game. And type the command `/statify sheetid [ID]`. (Replace [ID] with your Sheet's ID) and press Enter.
</div>
