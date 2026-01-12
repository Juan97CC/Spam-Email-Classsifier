// Function to request data from the server and display it in a table
function requestDataFromServer(url) {

  // Make a GET request to the provided URL
  fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'application/json' // Specify that we want JSON response
    }
  })
    .then(response => response.json()) // Parse the JSON response
    .then(data => {
      // Get the reference to the table where the data will be displayed
      let tableRef = document.getElementById("result-list");

      // Clear any existing content in the table
      tableRef.innerHTML = '';

      // Create a new table element
      let table = document.createElement('table');
      table.classList.add('data-table'); // Add a class for styling

      // Create and insert the table header
      let headerRow = table.insertRow();
      let headers = ["File", "Spam Probability Rounded", "Spam Probability", "Actual Class"];
      headers.forEach(headerText => {
        let header = document.createElement("th");
        header.textContent = headerText; // Set the header text
        headerRow.appendChild(header); // Append the header to the row
      });

      // Populate the table with the data
      data.forEach(item => {
        let row = table.insertRow();
        let cellFile = row.insertCell();
        cellFile.textContent = item.file; // Display file name
        let cellSpamProbRounded = row.insertCell();
        cellSpamProbRounded.textContent = item.spamProbRounded; // Display rounded spam probability
        let cellSpamProbability = row.insertCell();
        cellSpamProbability.textContent = item.spamProbability; // Display actual spam probability
        let cellActualClass = row.insertCell();
        cellActualClass.textContent = item.actualClass; // Display the actual class (spam/ham)
      });

      // Append the populated table to the table reference in the DOM
      tableRef.appendChild(table);
    })

    // Fetch the accuracy data
    .then(fetch("http://localhost:8080/spamDetector-1.0/api/spam/accuracy", {
        method: 'GET',
        headers: {
          'Accept': 'application/json' // Request accuracy in JSON format
        }
      })
        .then(response => response.json()) // Parse the JSON response
        .then(data => {
          console.log(data); // Log the accuracy data
          let div = document.getElementById('accuracy');
          div.innerHTML = data; // Display the accuracy in the appropriate div
        })
    )

    // Fetch the precision data
    .then(fetch("http://localhost:8080/spamDetector-1.0/api/spam/precision", {
        method: 'GET',
        headers: {
          'Accept': 'application/json' // Request precision in JSON format
        }
      })
        .then(response => response.json()) // Parse the JSON response
        .then(data => {
          console.log(data); // Log the precision data
          let div = document.getElementById('precision');
          div.innerHTML = data; // Display the precision in the appropriate div
        })
    )
    .catch(err => {
      // Catch and log any errors that occur during the fetch requests
      console.error("Error ", err);
    });
}

// Immediately invoke the function with the API URL as the argument
(function () {
  let api = 'http://localhost:8080/spamDetector-1.0/api/spam';
  requestDataFromServer(api);
})();
