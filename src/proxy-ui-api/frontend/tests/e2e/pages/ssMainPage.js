
var navigateCommands = {
  openClientsTab: function() {
    this.click('@clientsTab');
    return this;
  },
  openKeysTab: function() {
    this.click('@keysTab');
    return this;
  },
  openDiagnosticsTab: function() {
    this.click('@diagnosticsTab');
    return this;
  },
  openSettingsTab: function() {
    this.click('@settingsTab');
    return this;
  }
};

var clientTabCommands = {
  clickNameHeader: function() {
    this.click('@listNameHeader');
    return this;
  },
  clickIDHeader: function() {
    this.click('@listIDHeader');
    return this;
  },
  clickStatusHeader: function() {
    this.click('@listStatusHeader');
    return this;
  },
  openTestGov: function() {
    this.click('@testGovListItem');
    return this;
  },
  openTestService: function() {
    this.click('@testServiceListItem');
    return this;
  }
};

var clientInfoCommands = {
  openDetailsTab: function() {
    this.click('@detailsTab');
    return this;
  },
  openServiceClientsTab: function() {
    this.click('@serviceClientsTab');
    return this;
  },
  openServicesTab: function() {
    this.click('@servicesTab');
    return this;
  },
  openInternalServersTab: function() {
    this.click('@internalServersTab');
    return this;
  },
  openLocalGroupsTab: function() {
    this.click('@localGroupsTab');
    return this;
  }
}

var clientDetailsCommands = {
  openSignCertificateInfo: function() {
    this.click('@clientSignCertificate');
    return this;
  }
}

var certificatePopupCommands = {
  close: function() {
    this.click('@certificateInfoOKButton');
    return this;
  }
}

module.exports = {
  url: process.env.VUE_DEV_SERVER_URL,
  commands: [navigateCommands],
  elements: {
    clientsTab: { 
      selector: '//div[contains(@class, "v-tabs-bar__content")]//a[text()="Clients"]', 
      locateStrategy: 'xpath'},
    keysTab: { 
      selector: '//div[contains(@class, "v-tabs-bar__content")]//a[text()="Keys and certificates"]', 
      locateStrategy: 'xpath'},
    diagnosticsTab: { 
      selector: '//div[contains(@class, "v-tabs-bar__content")]//a[text()="Diagnostics"]', 
      locateStrategy: 'xpath'},
    settingsTab: { 
      selector: '//div[contains(@class, "v-tabs-bar__content")]//a[text()="Settings"]', 
      locateStrategy: 'xpath' },
    userMenuButton: { 
      selector: 'div.v-toolbar__content button .mdi-account-circle', 
      locateStrategy: 'css selector' }
  },
  sections: {
    clientsTab: {
      selector: '//div[contains(@class, "data-table-wrapper") and .//button[.//*[contains(text(), "add client")]]]',
      locateStrategy: 'xpath',
      commands: [clientTabCommands],
      elements: {
        addClientButton: { 
          selector: '//div[contains(@class, "v-btn__content") and text()="Add client"]',
          locateStrategy: 'xpath' },
        listNameHeader: { 
          selector: '//th[span[contains(text(),"Name")]]', 
          locateStrategy: 'xpath' },
        listIDHeader: { 
          selector: '//th[span[contains(text(),"ID")]]', 
          locateStrategy: 'xpath' },
        listStatusHeader: { 
          selector: '//th[span[contains(text(),"Status")]]', 
          locateStrategy: 'xpath' },
        testServiceListItem: { 
          selector: '//tbody//span[contains(text(),"TestService")]', 
          locateStrategy: 'xpath' },
        testGovListItem: { 
          selector: '//tbody//span[contains(text(),"TestGov")]', 
          locateStrategy: 'xpath' }
      }
    },
    clientInfo: {
      selector: 'h1.display-1',
      locateStrategy: 'css selector',
      commands: [clientInfoCommands],
      elements: {
        detailsTab: { 
          selector: '//div[contains(@class, "v-tabs-bar__content")]//a[contains(@class, "v-tab") and contains(text(), "details")]',
          locateStrategy: 'xpath' },
        serviceClientsTab: { 
          selector: '//div[contains(@class, "v-tabs-bar__content")]//a[contains(@class, "v-tab") and contains(text(), "service clients")]',
          locateStrategy: 'xpath' },
        servicesTab: { 
          selector: '//div[contains(@class, "v-tabs-bar__content")]//a[contains(@class, "v-tab") and contains(text(), "services")]',
          locateStrategy: 'xpath' },
        internalServersTab: { 
          selector: '//div[contains(@class, "v-tabs-bar__content")]//a[contains(@class, "v-tab") and contains(text(), "internal servers")]',
          locateStrategy: 'xpath' },
        localGroupsTab: { 
          selector: '//div[contains(@class, "v-tabs-bar__content")]//a[contains(@class, "v-tab") and contains(text(), "local groups")]',
          locateStrategy: 'xpath' }
      },
      sections: {
        details: {
          selector: '//div[contains(@class, "xrd-view-common") and .//*[contains(@class, "v-tab--active") and contains(text(), "details")]]',
          locateStrategy: 'xpath',
          commands: [clientDetailsCommands],
          elements: {
            clientSignCertificate: { 
              selector: 'span.cert-name',
              locateStrategy: 'css selector' }
          }      
        }
      }     
    },
    certificatePopup: {
      selector: '//*[contains(@class,"v-dialog--active") and .//*[contains(@class, "headline") and contains(text(),"Certificate")]]',
      locateStrategy: 'xpath',
      commands: [certificatePopupCommands],
      elements: {
        certificateInfoOKButton: { 
          selector: '.v-dialog--active button',
          locateStrategy: 'css selector' }
      }
    }
  }
};
