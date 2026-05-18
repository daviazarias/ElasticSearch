function sendQuery() {
    const query = document.getElementById('query').value;
    const queryURL = encodeURIComponent(query);
    const url = document.URL + "search?query=" + queryURL;
    fetch(url)
        .then(response => {
            if(!response.ok) {
                throw new Error('Não foi ok :(');
            }
            return response.json();
        })
        .then(data => showResults(data))
        .catch(error => console.error('Fetch error:', error));
}

const input = document.querySelector("input");
input.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
        event.preventDefault();
        sendQuery();
    }
});

const maxPageButtons = 10;
const pageButtons = document.getElementById("pagination");

const resultsList = document.getElementById("results")
function showResults(data) {
    resultsList.replaceChildren();
    pageButtons.replaceChildren();

    pageButtonsNumber = (data.numeroPaginas > maxPageButtons) ? maxPageButtons : data.numeroPaginas;

    for (let i = 1; i <= pageButtonsNumber; i++){
        var pageButtonContainer = document.createElement("li");

        var button = document.createElement("button");
        button.textContent = i;
        pageButtonContainer.appendChild(button);
        pageButtons.appendChild(pageButtonContainer);
    }

    for (let result of data.results) {
        var tituloContainer = document.createElement("dt");

        var link = document.createElement("a");
        link.textContent = result.title;
        link.href = result.url;
        link.target = "_blank";
        tituloContainer.appendChild(link);

        var abstractContainer = document.createElement("dd");
        abstractContainer.textContent = result.abs;

        resultsList.appendChild(tituloContainer);
        resultsList.appendChild(abstractContainer);
    }
}

document.querySelector('.pagination').addEventListener('click', (event) => {
    const botao = event.target.closest('button');
    if (!botao) return;

    pagina = botao.textContent;

    const query = document.getElementById('query').value;
    const queryURL = encodeURIComponent(query);
    const url = document.URL + "search?query=" + queryURL + "page=" + pagina;
    fetch(url)
        .then(response => {
            if(!response.ok) {
                throw new Error('Não foi ok :(');
            }
            return response.json();
        })
        .then(data => showResults(data))
        .catch(error => console.error('Fetch error:', error));
})