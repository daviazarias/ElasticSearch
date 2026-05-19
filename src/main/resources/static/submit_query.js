const maxPageButtons = 10;
const pageButtons = document.getElementById("pagination");
const resultsList = document.getElementById("results");
let currentPage = 1;

function sendQuery(page) {
    const query = document.getElementById('query').value;
    const queryURL = "query=" + encodeURIComponent(query);
    const pageURL = page ? ("&page=" + page) : ""
    const url = document.URL + "search?" + queryURL + pageURL;
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
        currentPage=1;
        sendQuery(null);
    }
});

function showResults(data) {
    resultsList.replaceChildren();

    displayPageButtons(data.numeroPaginas);

    for (let result of data.results) {
        const tituloContainer = document.createElement("dt");

        const link = document.createElement("a");
        link.textContent = result.title;
        link.href = result.url;
        link.target = "_blank";
        tituloContainer.appendChild(link);

        const abstractContainer = document.createElement("dd");
        abstractContainer.textContent = result.abs;

        resultsList.appendChild(tituloContainer);
        resultsList.appendChild(abstractContainer);
    }
}

function displayPageButtons(totalPages) {
    pageButtons.replaceChildren();

    const lesserHalfPageNumber = currentPage - maxPageButtons/2;
    const initialButton = (lesserHalfPageNumber > 0) ? lesserHalfPageNumber : 1;

    const pagesAmount = Math.min(maxPageButtons, totalPages - initialButton + 1);

    if(currentPage !== 1){
        const previousButton = document.createElement('button');
        previousButton.textContent = '« Anterior';
        previousButton.dataset.action = 'prev'
        pageButtons.appendChild(previousButton);
    }

    for (let i = 0; i < pagesAmount; i++){
        const pageButtonContainer = document.createElement("li");

        const button = document.createElement("button");
        button.textContent = initialButton + i;
        pageButtonContainer.appendChild(button);
        pageButtons.appendChild(pageButtonContainer);
    }

    if(currentPage !== totalPages){
        const nextButton = document.createElement('button');
        nextButton.textContent = 'Próximo »';
        nextButton.dataset.action = 'next';
        pageButtons.appendChild(nextButton);
    }
}

document.querySelector('.pagination').addEventListener('click', (event) => {
    const botao = event.target.closest('button');
    if (!botao) return;

    const acao = botao.dataset.action;

    if(acao === 'prev'){
        currentPage--;
    } else if(acao === 'next'){
        currentPage++;
    } else currentPage = botao.textContent;

    sendQuery(currentPage);
})