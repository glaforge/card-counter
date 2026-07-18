/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    total.textContent = "0";
    document.querySelector("#total-container").style.display = "none";
}

function clickPicker(e) {
    if (e) e.preventDefault();
    const picker = document.querySelector("#image-picker")
    picker.click();
}

function newFileSelection(e) {
    clearCardsAndPic();
    clearTotal();

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
            // Auto submit!
            clickSubmit(null);
        },
        false,
    );

    if (file) {
        reader.readAsDataURL(file);
    }
}

async function clickSubmit(e) {
    if (e) e.preventDefault();
    console.log("Auto-submit triggered");
    clearCards();
    clearTotal();

    const pickerButton = document.querySelector("#picker-button");
    pickerButton.loading = true;
    pickerButton.disabled = true;

    const picker = document.querySelector("#image-picker")
    var files = picker.files;
    if (!files.length && files.length != 1) {
        pickerButton.loading = false;
        pickerButton.disabled = false;
        return;
    }

    const file = picker.files[0];
    const formData = new FormData();
    formData.append('picture', file);

    console.log("Sending request");
    try {
        var response = await fetch("/upload", {
            method: "POST",
            body: formData
        });

        console.log("Received response");
        if (response.ok) {
            const cardNumbers = await response.json();
            console.log(cardNumbers);

            const cardsHolder = document.querySelector("#cards");
            
            cardNumbers.forEach((cardNumber, index) => {
                const card = document.createElement("div");
                card.classList.add("card-point");
                card.textContent = cardNumber;
                card.style.animationDelay = `${index * 0.05}s`;

                if (cardNumber === -1) {
                    card.classList.add("point-blue");
                } else if (cardNumber === 0) {
                    card.classList.add("point-cyan");
                } else if (cardNumber <= 4) {
                    card.classList.add("point-green");
                } else if (cardNumber <= 8) {
                    card.classList.add("point-yellow");
                } else {
                    card.classList.add("point-red");
                }
                cardsHolder.appendChild(card);
            });

            const total = cardNumbers.reduce((a, b) => a + b, 0);
            console.log("Total", total);
            const totalCard = document.querySelector("#total");
            totalCard.textContent = total;
            document.querySelector("#total-container").style.display = "inline-block";
            
            // Scroll to the bottom to see results
            setTimeout(() => {
                window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
            }, 100);

        } else {
            console.log("Error fetching server response");
        }
    } catch (err) {
        console.error("Failed to upload:", err);
    } finally {
        pickerButton.loading = false;
        pickerButton.disabled = false;
        pickerButton.innerHTML = `<sl-icon slot="prefix" name="camera"></sl-icon> Take Another Picture`;
    }
}