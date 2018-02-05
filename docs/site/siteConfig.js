/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

/* List of projects/orgs using your project for the users page */
const users = [
  {
    caption: 'dsngroup',
    infoLink: 'https://github.com/dsngroup',
    pinned: true,
  },
];

const siteConfig = {
  title: 'Broke' /* title for your website */,
  url: 'https://dsngroup.github.io/broke/docs/site/build/broke/' /* your website url */,
  baseUrl: 'https://dsngroup.github.io/broke/docs/site/build/broke/' /* base url for your project */,
  organizationName: 'dsngroup',
  projectName: 'broke',
  headerLinks: [
    {doc: 'introduction', label: 'User Guide'},
  ],
  users,
  /* colors for website */
  colors: {
    primaryColor: '#20455c',
    secondaryColor: "#3e7ca3"
  },
  // This copyright info is used in /core/Footer.js and blog rss/atom feeds.
  copyright:
    'Copyright © ' +
    new Date().getFullYear() +
    ' Dependable System and Network Lab, National Taiwan University',
  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks
    theme: 'default',
  },
  scripts: ['https://buttons.github.io/buttons.js'],
  // You may provide arbitrary config keys to be used as needed by your template.
  repoUrl: 'https://dsngroup.github.io/broke/docs/site/build/broke/',
};

module.exports = siteConfig;
