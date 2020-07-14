
module.exports = {
  tags: ['ss', 'clients', 'localgroups'],
  'Security server client local groups filtering': browser => {
    const frontPage = browser.page.ssFrontPage();
    const mainPage = browser.page.ssMainPage();
    const clientsTab = mainPage.section.clientsTab;
    const clientInfo = mainPage.section.clientInfo;
    const clientLocalGroups = clientInfo.section.localGroups;

    // Open SUT and check that page is loaded
    frontPage.navigate();
    browser.waitForElementVisible('//*[@id="app"]');

    // Enter valid credentials
    frontPage.signinDefaultUser();

    // Open TestGov Internal Servers
    mainPage.openClientsTab();
    browser.waitForElementVisible(clientsTab);
    clientsTab.openClient('TestService');
    browser.waitForElementVisible(clientInfo);
    clientInfo.openLocalGroupsTab();
    browser.waitForElementVisible(clientLocalGroups);

    // Verify default sorting
    clientLocalGroups.verifyGroupListRow(2, '1122');
    clientLocalGroups.verifyGroupListRow(3, '1212');
    clientLocalGroups.verifyGroupListRow(4, '2233');
    clientLocalGroups.verifyGroupListRow(5, 'abb');
    clientLocalGroups.verifyGroupListRow(6, 'bac');
    clientLocalGroups.verifyGroupListRow(7, 'cbb');

    // Change filtering and verify
    clientLocalGroups.filterBy('bb');
    clientLocalGroups.verifyGroupListRow(2, '1212');
    clientLocalGroups.verifyGroupListRow(3, 'abb');
    clientLocalGroups.verifyGroupListRow(4, 'cbb');

    // Change filtering and verify
    clientLocalGroups.filterBy('Desc');
    clientLocalGroups.verifyGroupListRow(2, '1122');
    clientLocalGroups.verifyGroupListRow(3, 'bac');

    browser.end();
  },
  'Security server client add local group': browser => {
    const frontPage = browser.page.ssFrontPage();
    const mainPage = browser.page.ssMainPage();
    const clientsTab = mainPage.section.clientsTab;
    const clientInfo = mainPage.section.clientInfo;
    const clientLocalGroups = clientInfo.section.localGroups;

    // Open SUT and check that page is loaded
    frontPage.navigate();
    browser.waitForElementVisible('//*[@id="app"]');

    // Enter valid credentials
    frontPage.signinDefaultUser();

    // Open TestGov Internal Servers
    mainPage.openClientsTab();
    browser.waitForElementVisible(clientsTab);
    clientsTab.openClient('TestService');
    browser.waitForElementVisible(clientInfo);
    clientInfo.openLocalGroupsTab();
    browser.waitForElementVisible(clientLocalGroups);

    // Cancel add local group dialog, verify that nothing happens
    clientLocalGroups.verifyGroupListRow(2, '1122');
    clientLocalGroups.verifyGroupListRow(3, '1212');
    clientLocalGroups.verifyGroupListRow(4, '2233');
    clientLocalGroups.verifyGroupListRow(5, 'abb');
    clientLocalGroups.verifyGroupListRow(6, 'bac');
    clientLocalGroups.verifyGroupListRow(7, 'cbb');

    clientLocalGroups.openAddDialog();
    clientLocalGroups.enterCode('abc');
    browser.assert.valueContains('//div[contains(@class, "dlg-edit-row") and .//*[contains(text(), "Code")]]//input', "abc");
    clientLocalGroups.enterDescription('addDesc');
    clientLocalGroups.cancelAddDialog();

    clientLocalGroups.verifyGroupListRow(2, '1122');
    clientLocalGroups.verifyGroupListRow(3, '1212');
    clientLocalGroups.verifyGroupListRow(4, '2233');
    clientLocalGroups.verifyGroupListRow(5, 'abb');
    clientLocalGroups.verifyGroupListRow(6, 'bac');
    clientLocalGroups.verifyGroupListRow(7, 'cbb');

    // Verify that local group dialog fields are empty after re-opening
    clientLocalGroups.openAddDialog();
    browser.assert.value('//div[contains(@class, "dlg-edit-row") and .//*[contains(text(), "Code")]]//input', "");
    browser.assert.value('//div[contains(@class, "dlg-edit-row") and .//*[contains(text(), "Description")]]//input', "");

    // Verify that add is disabled if only Code is entered
    clientLocalGroups.enterCode('abc');
    browser.waitForElementVisible('//button[contains(@data-test, "dialog-save-button") and @disabled="disabled"]');
    clientLocalGroups.cancelAddDialog();

    // Verify that add is disabled if only description is entered
    clientLocalGroups.openAddDialog();
    clientLocalGroups.enterDescription('addDesc');
    browser.waitForElementVisible('//button[contains(@data-test, "dialog-save-button") and @disabled="disabled"]');

    // Verify that trying to add a group with existing code results in an error message
    clientLocalGroups.enterCode('abb');
    clientLocalGroups.confirmAddDialog();
    browser.assert.containsText(mainPage.elements.snackBarMessage, "error_code.local_group_code_already_exists");
    mainPage.closeSnackbar();
 
    // Add a new group and verify
    clientLocalGroups.enterCode('abc');
    clientLocalGroups.enterDescription('addDesc');
    clientLocalGroups.confirmAddDialog();
    browser.assert.containsText(mainPage.elements.snackBarMessage, "Local group added");
    mainPage.closeSnackbar();

    clientLocalGroups.verifyGroupListRow(2, '1122');
    clientLocalGroups.verifyGroupListRow(3, '1212');
    clientLocalGroups.verifyGroupListRow(4, '2233');
    clientLocalGroups.verifyGroupListRow(5, 'abb');
    clientLocalGroups.verifyGroupListRow(6, 'abc');
    clientLocalGroups.verifyGroupListRow(7, 'bac');
    clientLocalGroups.verifyGroupListRow(8, 'cbb');

    browser.end();

  },
  'Security server add local group member': browser => {
    const frontPage = browser.page.ssFrontPage();
    const mainPage = browser.page.ssMainPage();
    const clientsTab = mainPage.section.clientsTab;
    const clientInfo = mainPage.section.clientInfo;
    const clientLocalGroups = clientInfo.section.localGroups;
    const localGroupPopup = mainPage.section.localGroupPopup;
    // Open SUT and check that page is loaded
    frontPage.navigate();
    browser.waitForElementVisible('//*[@id="app"]');

    // Enter valid credentials
    frontPage.signinDefaultUser();

    // Open TestGov Internal Servers and local group details view
    mainPage.openClientsTab();
    browser.waitForElementVisible(clientsTab);
    clientsTab.openClient('TestService');
    browser.waitForElementVisible(clientInfo);
    clientInfo.openLocalGroupsTab();
    browser.waitForElementVisible(clientLocalGroups);
    clientLocalGroups.openDetails('abb');
    browser.waitForElementVisible(localGroupPopup);

    // Add new member to local group, cancel case
    localGroupPopup.openAddMembers();
    localGroupPopup.searchMembers();
    localGroupPopup.selectNewTestComMember();
    localGroupPopup.cancelAddMembersDialog();

    browser.waitForElementNotVisible('//span[contains(@class, "headline") and contains(text(), "Add Members")]');
    browser.waitForElementVisible(localGroupPopup);
    browser.assert.not.elementPresent('//*[contains(text(),"TestCom")]')

    // Add new member to local group
    localGroupPopup.openAddMembers();
    localGroupPopup.searchMembers();
    localGroupPopup.selectNewTestComMember();
    localGroupPopup.addSelectedMembers();

    browser.waitForElementNotVisible('//span[contains(@class, "headline") and contains(text(), "Add Members")]');
    browser.waitForElementVisible(localGroupPopup);
    browser.assert.elementPresent('//*[contains(text(),"TestCom")]')

    browser.end();

  },
  'Security server delete local group members': browser => {
    const frontPage = browser.page.ssFrontPage();
    const mainPage = browser.page.ssMainPage();
    const clientsTab = mainPage.section.clientsTab;
    const clientInfo = mainPage.section.clientInfo;
    const clientLocalGroups = clientInfo.section.localGroups;
    const localGroupPopup = mainPage.section.localGroupPopup;
    // Open SUT and check that page is loaded
    frontPage.navigate();
    browser.waitForElementVisible('//*[@id="app"]');

    // Enter valid credentials
    frontPage.signinDefaultUser();

    // Open TestGov Internal Servers and local group details view
    mainPage.openClientsTab();
    browser.waitForElementVisible(clientsTab);
    clientsTab.openClient('TestService');
    browser.waitForElementVisible(clientInfo);
    clientInfo.openLocalGroupsTab();
    browser.waitForElementVisible(clientLocalGroups);
    clientLocalGroups.openDetails('bac');
    browser.waitForElementVisible(localGroupPopup);


    // Remove single 
    localGroupPopup.clickRemoveTestComMember();
    localGroupPopup.cancelMemberRemove();
    browser.waitForElementNotVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Remove member?")]');
    browser.assert.elementPresent('//*[contains(text(),"TestCom")]');

    localGroupPopup.clickRemoveTestComMember();
    localGroupPopup.confirmMemberRemove();
    browser.waitForElementNotVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Remove member?")]');
    browser.assert.not.elementPresent('//*[contains(text(),"TestCom")]');
    localGroupPopup.close();

    // Remove All
    clientLocalGroups.openDetails('bac');
    browser.waitForElementVisible(localGroupPopup);
    browser.assert.elementPresent('//*[contains(text(),"TestGov")]');
    browser.assert.elementPresent('//*[contains(text(),"TestOrg")]');

    localGroupPopup.clickRemoveAll();
    localGroupPopup.cancelMemberRemove();
    browser.waitForElementNotVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Remove all members?")]');
    browser.assert.elementPresent('//*[contains(text(),"TestGov")]');
    browser.assert.elementPresent('//*[contains(text(),"TestOrg")]');


    localGroupPopup.clickRemoveAll();
    browser.waitForElementVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Remove all members?")]');
    localGroupPopup.confirmMemberRemove();
    browser.waitForElementNotVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Remove all members?")]');
    browser.assert.not.elementPresent('//*[contains(text(),"TestGov")]');
    browser.assert.not.elementPresent('//*[contains(text(),"TestOrg")]');

    browser.end();

  },
  'Security server edit local group': browser => {
    const frontPage = browser.page.ssFrontPage();
    const mainPage = browser.page.ssMainPage();
    const clientsTab = mainPage.section.clientsTab;
    const clientInfo = mainPage.section.clientInfo;
    const clientLocalGroups = clientInfo.section.localGroups;
    const localGroupPopup = mainPage.section.localGroupPopup;
    // Open SUT and check that page is loaded
    frontPage.navigate();
    browser.waitForElementVisible('//*[@id="app"]');

    // Enter valid credentials
    frontPage.signinDefaultUser();

    // Open TestGov Internal Servers and local group details view
    mainPage.openClientsTab();
    browser.waitForElementVisible(clientsTab);
    clientsTab.openClient('TestService');
    browser.waitForElementVisible(clientInfo);
    clientInfo.openLocalGroupsTab();
    browser.waitForElementVisible(clientLocalGroups);
    clientLocalGroups.openDetails('cbb');
    browser.waitForElementVisible(localGroupPopup);

    // Change description
    localGroupPopup.changeDescription('');
    localGroupPopup.clickDescriptionLabel();
    browser.assert.containsText('//div[contains(@class, "v-snack__content")]', 'Validation failure');
    browser.assert.containsText(mainPage.elements.snackBarMessage, 'Validation failure');
    mainPage.closeSnackbar();
    localGroupPopup.close();
    browser.waitForElementVisible('//table[contains(@class, "details-certificates")]//tr[.//*[contains(text(),"cbb")] and .//*[contains(text(), "Group4")]]')
    clientLocalGroups.openDetails('cbb');
    browser.waitForElementVisible(localGroupPopup);
    localGroupPopup.changeDescription(browser.globals.test_string_300.slice(0,256));
    localGroupPopup.clickDescriptionLabel();
    browser.assert.containsText('//div[contains(@class, "v-snack__content")]', 'Validation failure');
    browser.assert.containsText(mainPage.elements.snackBarMessage, 'Validation failure');
    mainPage.closeSnackbar();
    localGroupPopup.close();
    browser.waitForElementVisible('//table[contains(@class, "details-certificates")]//tr[.//*[contains(text(),"cbb")] and .//*[contains(text(), "Group4")]]');
    clientLocalGroups.openDetails('cbb');
    browser.waitForElementVisible(localGroupPopup);
    localGroupPopup.changeDescription(browser.globals.test_string_300.slice(0,255));
    localGroupPopup.clickDescriptionLabel();
    browser.assert.containsText('//div[contains(@class, "v-snack__content")]', 'Description saved');
    browser.assert.containsText(mainPage.elements.snackBarMessage, 'Description saved');
    mainPage.closeSnackbar();
    localGroupPopup.close();
    browser.waitForElementVisible('//table[contains(@class, "details-certificates")]//tr[.//*[contains(text(),"cbb")] and .//*[contains(text(), "'+browser.globals.test_string_300.slice(0,255)+'")]]')
    clientLocalGroups.openDetails('cbb');
    browser.waitForElementVisible(localGroupPopup);
    localGroupPopup.changeDescription('GroupChanged');
    localGroupPopup.clickDescriptionLabel();
    browser.assert.containsText('//div[contains(@class, "v-snack__content")]', 'Description saved');
    browser.assert.containsText(mainPage.elements.snackBarMessage, 'Description saved');
    mainPage.closeSnackbar();
    localGroupPopup.close();
    browser.waitForElementVisible('//table[contains(@class, "details-certificates")]//tr[.//*[contains(text(),"cbb")] and .//*[contains(text(), "GroupChanged")]]')
    browser.end();

  },
  'Security server delete local group': browser => {
    const frontPage = browser.page.ssFrontPage();
    const mainPage = browser.page.ssMainPage();
    const clientsTab = mainPage.section.clientsTab;
    const clientInfo = mainPage.section.clientInfo;
    const clientLocalGroups = clientInfo.section.localGroups;
    const localGroupPopup = mainPage.section.localGroupPopup;

    // Open SUT and check that page is loaded
    frontPage.navigate();
    browser.waitForElementVisible('//*[@id="app"]');

    // Enter valid credentials
    frontPage.signinDefaultUser();

    // Open TestGov Internal Servers and local group details view
    mainPage.openClientsTab();
    browser.waitForElementVisible(clientsTab);
    clientsTab.openClient('TestService');
    browser.waitForElementVisible(clientInfo);
    clientInfo.openLocalGroupsTab();
    browser.waitForElementVisible(clientLocalGroups);

    // Delete and confirm
    browser.assert.elementPresent('//table[contains(@class, "details-certificates")]//tr[.//*[contains(text(),"bac")]]')
    clientLocalGroups.openDetails('bac');
    browser.waitForElementVisible(localGroupPopup);
    browser.waitForElementVisible(localGroupPopup.elements.localGroupPopupCloseButton);
    localGroupPopup.deleteThisGroup();
    browser.waitForElementVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Delete group?")]');
    localGroupPopup.confirmDelete();
    browser.waitForElementVisible(clientLocalGroups);
    browser.assert.not.elementPresent('//table[contains(@class, "details-certificates")]//tr[.//*[contains(text(),"bac")]]')

    // Delete and cancel
    clientLocalGroups.openDetails('cbb');
    browser.waitForElementVisible(localGroupPopup);
    localGroupPopup.deleteThisGroup();
    browser.waitForElementVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Delete group?")]');
    localGroupPopup.cancelDelete();
    browser.waitForElementNotVisible('//*[contains(@data-test, "dialog-title") and contains(text(), "Delete group?")]');   
    localGroupPopup.close();
    browser.waitForElementVisible('//table[contains(@class, "details-certificates")]//tr[.//*[contains(text(),"cbb")]]')

  }
};
