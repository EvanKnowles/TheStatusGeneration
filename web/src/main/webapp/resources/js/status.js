$(function () {
    $(document).on('click', '.more-statuses', function () {
      var $this = $(this);
        $this.find('span').text('Please wait, fetching more...');
        $this.attr('disabled', 'disabled');
    });

    $(document).on('click', '.see-another', function () {
      var $this = $(this);
        $this.find('span').text('Grabbing another one...');
        $this.attr('disabled', 'disabled');
    });

    $(document).on('click', '.done-picking', function () {
        PF('mashUpWidget').hide();
    });

    $(document).on('click', '.opt-button.mash-up', function () {
        PF('mashUpWidget').show();
    });

    $(document).on('click', '.mash-choice', function () {
        if ($(this).attr('class').indexOf('selected') == -1) {
            $(this).addClass('selected');
        } else {
            $(this).removeClass('selected');
        }
    });
    $(document).on('click', '.opt-in', function () {
        toggleStatus();
    });

    $(document).on('click', '.sanity-level > div', function () {
        var $this = $(this);
        $('.selected').removeClass('selected');
        var $parent = $this.parents('.sanity-level');
        var $input = $parent.find('input');

        var sanity = $this.attr('class');
        if (sanity.indexOf('low') != -1) {
            $input.val(1);
        } else if (sanity.indexOf('medium') != -1) {
            $input.val(2);
        } else if (sanity.indexOf('high') != -1) {
            $input.val(3);
        }
        $input.change();

        $this.addClass('selected');
    });
});