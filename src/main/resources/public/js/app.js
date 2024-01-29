function clearCards() {
    const cardsHolder = document.querySelector("#cards");
    cardsHolder.replaceChildren();
}

function clearPic() {
    const preview = document.querySelector("#image-preview");
    preview.style.display = "none";
    preview.src = "";
}

function clearCardsAndPic() {
    clearPic();
    clearCards();
}

function clearTotal() {
    const total = document.querySelector("#total");
    total.textContent = "";
}

function clickPicker(e) {
    clearCardsAndPic();

    const picker = document.querySelector("#image-picker")
    picker.click(e);
}

function newFileSelection(e) {
    clearCardsAndPic();

    const picker = document.querySelector("#image-picker");
    var files = picker.files;
    if (!files.length && files.length != 1) return;

    const preview = document.querySelector("#image-preview");
    const file = picker.files[0];
    const reader = new FileReader();

    reader.addEventListener(
        "load",
        () => {
            preview.src = reader.result;
            preview.style.display = "block";
        },
        false,
    );

    if (file) {
        reader.readAsDataURL(file);
    }
}

async function clickSubmit(e) {
    console.log("Submit clicked");
    clearCards();
    clearTotal();

    const submitButton = document.querySelector("#submit-button");
    submitButton.loading = true;
    submitButton.disabled = true;

    const picker = document.querySelector("#image-picker")
    var files = picker.files;
    if (!files.length && files.length != 1) return;

    const file = picker.files[0];

    const formData = new FormData();
    formData.append('picture', file);

    console.log("Sending request");
    var response = await fetch("/upload", {
        method: "POST",
        body: formData
    });

    submitButton.loading = false;
    submitButton.disabled = false;

    console.log("Received response");
    if (response.ok) {
        const cardNumbers = await response.json();
        console.log(cardNumbers);

        cardNumbers.forEach(cardNumber => {
            const cardsHolder = document.querySelector("#cards");
            const card = document.createElement("sl-tag");
            card.textContent = cardNumber;
            card.setAttribute("size", "large");
            if (cardNumber <= 0) {
                card.setAttribute("variant", "primary");
            } else if (cardNumber <= 4) {
                card.setAttribute("variant", "success");
            } else if (cardNumber <= 8) {
                card.setAttribute("variant", "warning");
            } else {
                card.setAttribute("variant", "danger");
            }
            cardsHolder.appendChild(card);
        });

        const total = cardNumbers.reduce((a, b) => a + b, 0);
        console.log("Total", total);
        const totalCard = document.querySelector("#total");
        totalCard.textContent = total;
    } else {
        console.log("Error fetching server response");
    }
}