// Highlight selected option
document.querySelectorAll('.option-label').forEach(label => {
    const radio = label.querySelector('input[type=radio]');

    // Highlight already-selected on page load
    if (radio.checked) {
        label.classList.add('selected');
    }

    label.addEventListener('click', () => {
        // Remove selected from all
        document.querySelectorAll('.option-label').forEach(l => l.classList.remove('selected'));
        // Add to clicked
        label.classList.add('selected');
    });
});

// Client-side validation before submit
document.getElementById('quizForm').addEventListener('submit', function(e) {
    const selected = document.querySelector('input[name="answer"]:checked');
    if (!selected) {
        e.preventDefault();
        // Show inline error
        let err = document.querySelector('.quiz-error');
        if (!err) {
            err = document.createElement('div');
            err.className = 'quiz-error';
            const card = document.querySelector('.question-card');
            card.parentNode.insertBefore(err, card);
        }
        err.textContent = 'You must select an answer before proceeding.';
        err.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
});
