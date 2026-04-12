/**
 * categories.js — page-specific JS for categories.html
 *
 * Wires the "Імпортувати з Excel" button to a hidden <input type="file">,
 * then auto-submits the hidden import form when a file is chosen.
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var importBtn   = document.getElementById('categoryImportBtn');
        var fileInput   = document.getElementById('categoryImportInput');
        var importForm  = document.getElementById('categoryImportForm');

        if (!importBtn || !fileInput || !importForm) return;

        // Clicking the visible button triggers the hidden file input
        importBtn.addEventListener('click', function () {
            fileInput.click();
        });

        // Once the user picks a file, submit the form automatically
        fileInput.addEventListener('change', function () {
            if (fileInput.files && fileInput.files.length > 0) {
                importForm.submit();
            }
        });
    });
}());
