import argparse
import logging

def parse_flags():
    parser = argparse.ArgumentParser(description="Flags to start the bot with.")
    parser.add_argument('--debug', help="Start the bot into debug mode.", action="store_true")
    parser.add_argument('--test-run', help="For CI testing", action="store_true")
    parser.add_argument('--log-to-file', help="Write logs to file", action="store_true")

    try:
        args = parser.parse_args()
    except Exception as e:
        # Logging has not been setup yet. So we can only print.
        print(e)
        exit(0)

    if args.debug:
        logging.basicConfig(level=logging.DEBUG)

    if args.log_to_file:
        rickbot_log = logging.basicConfig(filename="log.log",
                                          filemode="w",
                                          format="[%(asctime)s] %(msecs)d %(name)s %(levelname)s %(message)s",
                                          datefmt="'%H:%M:%S'",
                                          level=logging.DEBUG)

    if args.test_run:
        logging.basicConfig(level=logging.DEBUG)
        print("Running for CI")