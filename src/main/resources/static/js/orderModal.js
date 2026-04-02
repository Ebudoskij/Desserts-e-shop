function openOrderModal(productId, productName, isCustomizable) {
    document.body.style.overflow = 'hidden';
    document.getElementById('orderProductId').value = productId;
    document.getElementById('orderModalTitle').innerText = 'Додати в кошик: ' + productName;
    
    const aiContainer = document.getElementById('additionalItemsContainer');
    if (isCustomizable === 'true' || isCustomizable === true) {
        aiContainer.style.display = 'block';
        // Reset to default (No decor)
        const noDecorRadio = document.querySelector('input[name="additionalItemId"][value=""]');
        if (noDecorRadio) noDecorRadio.checked = true;
    } else {
        aiContainer.style.display = 'none';
        const noDecorRadio = document.querySelector('input[name="additionalItemId"][value=""]');
        if (noDecorRadio) noDecorRadio.checked = true;
    }
    
    toggleOrderCustomDecorFields();
    document.getElementById('orderModal').style.display = 'flex';
}

function closeOrderModal() {
    document.body.style.overflow = '';
    document.getElementById('orderModal').style.display = 'none';
    document.getElementById('orderForm').reset();
    toggleOrderCustomDecorFields();
}

function toggleOrderCustomDecorFields() {
    const defaultRadio = document.querySelector('input[name="additionalItemId"]:checked');
    const value = defaultRadio ? defaultRadio.value : "";
    const customFields = document.getElementById('customDecorFields');
    const customImagesInput = document.getElementById('customImages');
    
    const isCustom = value === 'CUSTOM';
    document.getElementById('orderCustomDecor').value = isCustom;
    
    if (isCustom) {
        customFields.style.display = 'block';
        // We set value to empty string before submit to avoid sending 'CUSTOM' as Long
        defaultRadio.value = "";
    } else {
        customFields.style.display = 'none';
        const customRadio = document.querySelector('input[name="additionalItemId"][value=""]');
        if (customRadio && document.querySelector('input[name="additionalItemId"]:checked') !== customRadio) {
            // Restore missing CUSTOM value if it was wiped
            const customRadioOriginal = Array.from(document.querySelectorAll('input[name="additionalItemId"]')).find(r => r.value === "" && Array.from(document.querySelectorAll('input[name="additionalItemId"][value=""]')).length > 1);
            if (customRadioOriginal) customRadioOriginal.value = "CUSTOM";
        }
    }
}

